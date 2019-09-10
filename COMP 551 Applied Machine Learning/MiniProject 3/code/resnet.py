# -*- coding: utf-8 -*-
"""
Created on Sat Mar  9 14:58:42 2019

@author: Max
"""

from __future__ import division

import six
from keras.models import Model
from keras.layers import (
    Input,
    Activation,
    Dense,
    Flatten
)
from keras.layers.convolutional import (
    Conv2D,
    MaxPooling2D,
    AveragePooling2D
)
from keras.layers.merge import add
from keras.layers.normalization import BatchNormalization
from keras.regularizers import l2
from keras import backend as K
import pickle
import numpy as np
from keras.utils import np_utils
from sklearn.model_selection import train_test_split
import csv
from keras import optimizers
from sklearn.model_selection import KFold #Import K-Fold validation from SKlearn

def _bn_relu(input):
    """Helper to build a BN -> relu block
    """
    norm = BatchNormalization(axis=CHANNEL_AXIS)(input)
    return Activation("relu")(norm)


def _conv_bn_relu(**conv_params):
    """Helper to build a conv -> BN -> relu block
    """
    filters = conv_params["filters"]
    kernel_size = conv_params["kernel_size"]
    strides = conv_params.setdefault("strides", (1, 1))
    kernel_initializer = conv_params.setdefault("kernel_initializer", "he_normal")
    padding = conv_params.setdefault("padding", "same")
    kernel_regularizer = conv_params.setdefault("kernel_regularizer", l2(1.e-4))

    def f(input):
        conv = Conv2D(filters=filters, kernel_size=kernel_size,
                      strides=strides, padding=padding,
                      kernel_initializer=kernel_initializer,
                      kernel_regularizer=kernel_regularizer)(input)
        return _bn_relu(conv)

    return f


def _bn_relu_conv(**conv_params):
    """Helper to build a BN -> relu -> conv block.
    This is an improved scheme proposed in http://arxiv.org/pdf/1603.05027v2.pdf
    """
    filters = conv_params["filters"]
    kernel_size = conv_params["kernel_size"]
    strides = conv_params.setdefault("strides", (1, 1))
    kernel_initializer = conv_params.setdefault("kernel_initializer", "he_normal")
    padding = conv_params.setdefault("padding", "same")
    kernel_regularizer = conv_params.setdefault("kernel_regularizer", l2(1.e-4))

    def f(input):
        activation = _bn_relu(input)
        return Conv2D(filters=filters, kernel_size=kernel_size,
                      strides=strides, padding=padding,
                      kernel_initializer=kernel_initializer,
                      kernel_regularizer=kernel_regularizer)(activation)

    return f


def _shortcut(input, residual):
    """Adds a shortcut between input and residual block and merges them with "sum"
    """
    # Expand channels of shortcut to match residual.
    # Stride appropriately to match residual (width, height)
    # Should be int if network architecture is correctly configured.
    input_shape = K.int_shape(input)
    residual_shape = K.int_shape(residual)
    stride_width = int(round(input_shape[ROW_AXIS] / residual_shape[ROW_AXIS]))
    stride_height = int(round(input_shape[COL_AXIS] / residual_shape[COL_AXIS]))
    equal_channels = input_shape[CHANNEL_AXIS] == residual_shape[CHANNEL_AXIS]

    shortcut = input
    # 1 X 1 conv if shape is different. Else identity.
    if stride_width > 1 or stride_height > 1 or not equal_channels:
        shortcut = Conv2D(filters=residual_shape[CHANNEL_AXIS],
                          kernel_size=(1, 1),
                          strides=(stride_width, stride_height),
                          padding="valid",
                          kernel_initializer="he_normal",
                          kernel_regularizer=l2(0.0001))(input)

    return add([shortcut, residual])


def _residual_block(block_function, filters, repetitions, is_first_layer=False):
    """Builds a residual block with repeating bottleneck blocks.
    """
    def f(input):
        for i in range(repetitions):
            init_strides = (1, 1)
            if i == 0 and not is_first_layer:
                init_strides = (2, 2)
            input = block_function(filters=filters, init_strides=init_strides,
                                   is_first_block_of_first_layer=(is_first_layer and i == 0))(input)
        return input

    return f


def basic_block(filters, init_strides=(1, 1), is_first_block_of_first_layer=False):
    """Basic 3 X 3 convolution blocks for use on resnets with layers <= 34.
    Follows improved proposed scheme in http://arxiv.org/pdf/1603.05027v2.pdf
    """
    def f(input):

        if is_first_block_of_first_layer:
            # don't repeat bn->relu since we just did bn->relu->maxpool
            conv1 = Conv2D(filters=filters, kernel_size=(3, 3),
                           strides=init_strides,
                           padding="same",
                           kernel_initializer="he_normal",
                           kernel_regularizer=l2(1e-4))(input)
        else:
            conv1 = _bn_relu_conv(filters=filters, kernel_size=(3, 3),
                                  strides=init_strides)(input)

        residual = _bn_relu_conv(filters=filters, kernel_size=(3, 3))(conv1)
        return _shortcut(input, residual)

    return f


def bottleneck(filters, init_strides=(1, 1), is_first_block_of_first_layer=False):
    """Bottleneck architecture for > 34 layer resnet.
    Follows improved proposed scheme in http://arxiv.org/pdf/1603.05027v2.pdf
    Returns:
        A final conv layer of filters * 4
    """
    def f(input):

        if is_first_block_of_first_layer:
            # don't repeat bn->relu since we just did bn->relu->maxpool
            conv_1_1 = Conv2D(filters=filters, kernel_size=(1, 1),
                              strides=init_strides,
                              padding="same",
                              kernel_initializer="he_normal",
                              kernel_regularizer=l2(1e-4))(input)
        else:
            conv_1_1 = _bn_relu_conv(filters=filters, kernel_size=(1, 1),
                                     strides=init_strides)(input)

        conv_3_3 = _bn_relu_conv(filters=filters, kernel_size=(3, 3))(conv_1_1)
        residual = _bn_relu_conv(filters=filters * 4, kernel_size=(1, 1))(conv_3_3)
        return _shortcut(input, residual)

    return f


def _handle_dim_ordering():
    global ROW_AXIS
    global COL_AXIS
    global CHANNEL_AXIS
    if K.image_dim_ordering() == 'tf':
        ROW_AXIS = 1
        COL_AXIS = 2
        CHANNEL_AXIS = 3
    else:
        CHANNEL_AXIS = 1
        ROW_AXIS = 2
        COL_AXIS = 3


def _get_block(identifier):
    if isinstance(identifier, six.string_types):
        res = globals().get(identifier)
        if not res:
            raise ValueError('Invalid {}'.format(identifier))
        return res
    return identifier


class ResnetBuilder(object):
    @staticmethod
    def build(input_shape, num_outputs, block_fn, repetitions,pools,stride,ksize):
        """Builds a custom ResNet like architecture.
        Args:
            input_shape: The input shape in the form (nb_channels, nb_rows, nb_cols)
            num_outputs: The number of outputs at final softmax layer
            block_fn: The block function to use. This is either `basic_block` or `bottleneck`.
                The original paper used basic_block for layers < 50
            repetitions: Number of repetitions of various block units.
                At each block unit, the number of filters are doubled and the input size is halved
        Returns:
            The keras `Model`.
        """
        _handle_dim_ordering()
        if len(input_shape) != 3:
            raise Exception("Input shape should be a tuple (nb_channels, nb_rows, nb_cols)")

        # Permute dimension order if necessary
        if K.image_dim_ordering() == 'tf':
            input_shape = (input_shape[1], input_shape[2], input_shape[0])

        # Load function from str if needed.
        block_fn = _get_block(block_fn)

        input = Input(shape=input_shape)
        conv1 = _conv_bn_relu(filters=64, kernel_size=(ksize, ksize), strides=(2, 2))(input)
        pool1 = MaxPooling2D(pool_size=(pools, pools), strides=(stride, stride), padding="same")(conv1)

        block = pool1
        filters = 64
        for i, r in enumerate(repetitions):
            block = _residual_block(block_fn, filters=filters, repetitions=r, is_first_layer=(i == 0))(block)
            filters *= 2

        # Last activation
        block = _bn_relu(block)

        # Classifier block
        block_shape = K.int_shape(block)
        pool2 = AveragePooling2D(pool_size=(block_shape[ROW_AXIS], block_shape[COL_AXIS]),
                                 strides=(1, 1))(block)
        flatten1 = Flatten()(pool2)
        dense = Dense(units=num_outputs, kernel_initializer="he_normal",
                      activation="softmax")(flatten1)

        model = Model(inputs=input, outputs=dense)
        return model

    @staticmethod
    def build_resnet_18(input_shape, num_outputs,pools,stride,ksize):
        return ResnetBuilder.build(input_shape, num_outputs, basic_block, [2, 2, 2, 2],pools,stride,ksize)

    @staticmethod
    def build_resnet_34(input_shape, num_outputs,pools,stride,ksize):
        return ResnetBuilder.build(input_shape, num_outputs, basic_block, [3, 4, 6, 3],pools,stride,ksize)

    @staticmethod
    def build_resnet_50(input_shape, num_outputs):
        return ResnetBuilder.build(input_shape, num_outputs, bottleneck, [3, 4, 6, 3])

    @staticmethod
    def build_resnet_101(input_shape, num_outputs,pools,stride,ksize):
        return ResnetBuilder.build(input_shape, num_outputs, bottleneck, [3, 4, 23, 3],pools,stride,ksize)

    @staticmethod
    def build_resnet_152(input_shape, num_outputs,pools,stride,ksize):
        return ResnetBuilder.build(input_shape, num_outputs, bottleneck, [3, 8, 36, 3],pools,stride,ksize)

print("[INFO] compiling model...")

def Tune(batch,pool,learn,ksize,fold):
    # Data input
    try:   
        with open('Xtrain.txt','rb') as x_file:
            X_train=pickle.load(x_file)
        with open('ytrain.txt','rb') as y_file:
            y_train=pickle.load(y_file)
        with open('Xtest.txt','rb') as x_test_file:
            X_test=pickle.load(x_test_file)
    except IOError as err:  
        print('File error: ' + str(err))
    
    numClass = 10
    input_shape = (1,64,64)
    epochs = 30
    stride = 2 #Default 2
    
    X_train = np.divide(X_train, 255.0)
    X_train = X_train[:,:,:,np.newaxis]
    X_test = np.divide(X_test, 255.0)
    X_test = X_test[:,:,:,np.newaxis]
    y_train = np.array(y_train)
    
    kf = KFold(n_splits = fold,shuffle=True)
    result = []
    
    for train_index, val_index in kf.split(X_train):
        trainData, valData = X_train[train_index,:,:,:], X_train[val_index,:,:,:]
        trainLabels, valLabels = y_train[train_index],y_train[val_index]
        
        #(trainData, valData, trainLabels, valLabels) = train_test_split(X_train, y_train, test_size=0.1, shuffle=True)
        trainLabels_next = np_utils.to_categorical(trainLabels, numClass)
        valLabels_next = np_utils.to_categorical(valLabels, numClass)   
        
        model = ResnetBuilder.build_resnet_18(input_shape,numClass,pool,stride,ksize)
        adam = optimizers.Adam(lr=learn, beta_1=0.99, beta_2=0.999, epsilon=None, decay=0.0, amsgrad=False)
        #sgd = optimizers.SGD(lr=0.01, momentum=0.9, decay=0.0, nesterov=False)
        
        model.compile(loss='categorical_crossentropy',
                      optimizer=adam,
                      metrics=['accuracy'])
        
        model.fit(trainData, trainLabels_next, batch_size=batch, shuffle=True, epochs=epochs, validation_data=(valData, valLabels_next))
        
        scores = model.evaluate(valData, valLabels_next, verbose=0)
        
        y_test = model.predict(X_test, batch_size=batch)
        result.append(y_test)
    
    ensy_test = np.sum(np.array(result),axis=0)
    y_test_single = []
    
    for i in range(ensy_test.shape[0]):
        y_test_single.append(np.argmax(ensy_test[i,:]))
        
    #Ouput result to csv file
    with open("result_resnet18_ensemble.csv",'w',newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(['Id','Category'])
        i = 0
        for label in y_test_single:
            writer.writerow([i,int(y_test_single[i])])
            i+=1
    return scores[1]

#batchlist = [32,256]
#poolist = [2,3]
#learnlist = [0.001,1e-4,1e-5]
#ksizelist = [3,4,7]
#
#ba,po,le,ks,ta = 0,0,0,0,0
#for i in range(2):
#    for j in range(2):
#        for k in range(3):
#            for l in range(3):
#                mark = Tune(batchlist[i],poolist[j],learnlist[k],ksizelist[l])
#                if mark > ta:
#                    ta = mark
#                    ba,po,le,ks = batchlist[i],poolist[j],learnlist[k],ksizelist[l]
    


 #best combination is 32,3,0.001,0.99
 #best combination is 32,2,0.001,7   

Tune(32,2,0.001,7,10)