# -*- coding: utf-8 -*-
"""
Created on Sun Mar 10 17:40:03 2019

@author: Max
"""
import cv2
import numpy as np
import pandas as pd
import pickle
import csv
#from PIL import Image
from scipy import ndimage

train_images = pd.read_pickle('train_images.pkl.zip')
test_images = pd.read_pickle('test_images.pkl.zip')

def Binary(pixs,num):
    # threshold to get just the signature (INVERTED)
    raw = pixs[num,:,:]
    #cv2.imwrite("Raw.jpg", raw)

    imag = raw.astype(np.uint8)
    # retval, thresh_gray = cv2.threshold(imag, 254, maxval=255, \
    #                                    type=cv2.THRESH_BINARY_INV)
    # thresh_gray = 255 - thresh_gray
    # label_im, nb_labels = ndimage.label(thresh_gray)
     
    #cv2.imwrite("Greyed.jpg", thresh_gray)
    return imag

train_raw, test_raw = [],[]

for j in range(len(train_images)):
    train_raw.append(np.array(Binary(train_images,j))) 

for j in range(len(test_images)):
    test_raw.append(np.array(Binary(test_images,j)))
    
with open('train_labels.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    train_label = [row[1] for row in reader]
train_label = [int(label) for label in train_label[1:]]

try:
    with open('Xtrain.txt','wb') as x_train_file:
        pickle.dump(train_raw,x_train_file)
    with open('Ytrain.txt','wb') as y_trainfile:
        pickle.dump(train_label,y_trainfile)
    with open('Xtest.txt','wb') as x_test_file:
        pickle.dump(test_raw,x_test_file)
except IOError as err:  
    print('File error: ' + str(err))

#for j in range(len(train_images)):
#    if j>100:
#        break
#    pic = Image.fromarray(train_raw[j])
#    pic.save(str(j)+'_'+str(train_label[j])+'.jpg')


