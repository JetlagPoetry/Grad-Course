# USAGE
# python lenet_mnist.py --save-model 1 --weights output/lenet_weights.hdf5
# python lenet_mnist.py --load-model 1 --weights output/lenet_weights.hdf5

# import the necessary packages
# from pyimagesearch.cnn.networks.lenet import LeNet
from sklearn.model_selection import train_test_split
from sklearn import datasets
from keras.optimizers import SGD, Adam, RMSprop
from keras.utils import np_utils
from keras.preprocessing.image import ImageDataGenerator
from sklearn.model_selection import KFold
import numpy as np
import argparse
import cv2
import pickle
import csv
import matplotlib.pyplot as plt
import random
from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, BatchNormalization
from keras.layers import Conv2D
from keras.layers.pooling import MaxPooling2D
from keras.layers import Flatten
from keras import regularizers

def lenet(_numClass, _datasize):
	numClass = _numClass
	datasize = _datasize
	opt = RMSprop()

	model = Sequential()

	# first set of CONV => RELU => POOL
	model.add(Conv2D(20, 5, 5, border_mode="same",
		input_shape=(datasize, datasize, 1)))
	model.add(Activation("relu"))
	model.add(MaxPooling2D(pool_size=(2, 2), strides=(2, 2), dim_ordering="tf"))

	# second set of CONV => RELU => POOL
	model.add(Conv2D(50, 5, 5, border_mode="same"))
	model.add(Activation("relu"))
	model.add(MaxPooling2D(pool_size=(2, 2), strides=(2, 2), dim_ordering="tf"))

	# set of FC => RELU layers
	model.add(Flatten())
	model.add(Dense(500))
	model.add(Activation("relu"))

	# softmax classifier
	model.add(Dense(numClass))
	model.add(Activation("softmax"))

	model.compile(loss="categorical_crossentropy", optimizer=opt,
		metrics=["accuracy"])
	return model


# Data input
try:   
    with open('Xtrain.txt','rb') as x_file:
        X_train=pickle.load(x_file)
    with open('Ytrain.txt','rb') as y_file:
        y_train=pickle.load(y_file)
    with open('Xtest.txt','rb') as x_test_file:
        X_test=pickle.load(x_test_file)
except IOError as err:  
    print('File error: ' + str(err))

numClass = 10
datasize = 64
epochs = 20
batch_size = 128

X_train = np.divide(X_train, 255.0)
X_train = X_train[:,:,:,np.newaxis]
X_test = np.divide(X_test, 255.0)
X_test = X_test[:,:,:,np.newaxis]
y_train = np.array(y_train)

(trainData, testData, trainLabels, testLabels) = train_test_split(X_train, y_train, test_size=0.33, shuffle=True)

y_train = np_utils.to_categorical(y_train, numClass)
trainLabels = np_utils.to_categorical(trainLabels, numClass)
testLabels = np_utils.to_categorical(testLabels, numClass)	


print("[INFO] compiling model...")
# opt = SGD(lr=0.01)

model = lenet(numClass, datasize)
model.fit(trainData, trainLabels, batch_size=batch_size, shuffle=True, epochs=epochs, verbose=1, validation_data=(testData, testLabels))

# y_test = model.predict_classes(X_test, batch_size=batch_size)
# print(y_test)
# #Ouput result to csv file
# with open("result.csv",'w',newline='') as csvfile:
#     writer = csv.writer(csvfile)
#     writer.writerow(['Id','Category'])
#     i = 0
#     for label in y_test:
#         writer.writerow([i,int(y_test[i])])
#         i+=1
