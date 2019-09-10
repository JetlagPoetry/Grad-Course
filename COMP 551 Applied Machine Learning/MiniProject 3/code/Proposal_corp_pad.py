# -*- coding: utf-8 -*-
"""
Created on Thu Feb 28 09:09:45 2019

@author: Max
"""

import cv2
import numpy as np
import pandas as pd
from PIL import Image
import pickle
import csv
from scipy import ndimage

train_images = pd.read_pickle('train_images.pkl.zip')
test_images = pd.read_pickle('test_images.pkl.zip')

def BigFigure(pixs,num,cont_blank):
    # threshold to get just the signature (INVERTED)
    raw = pixs[num,:,:]
    imag = raw.astype(np.uint8)
    retval, thresh_gray = cv2.threshold(imag, 240, maxval=255, \
                                       type=cv2.THRESH_BINARY_INV)
    
    # image, contours, hier = cv2.findContours(thresh_gray, cv2.RETR_TREE,
    #                 cv2.CHAIN_APPROX_SIMPLE)
    contours, hier = cv2.findContours(thresh_gray, cv2.RETR_TREE,
                cv2.CHAIN_APPROX_SIMPLE)
     
    cont_filt = [contours[i] for i in range(len(contours)) if contours[i].shape[0] >= cont_blank] 
    
    xx,yy,ww,hh,area = 0,0,0,0,0
    for c in cont_filt:
        # get the bounding rect
        x, y, w, h = cv2.boundingRect(c)
       
        # get the contour area
        # sizy = cv2.contourArea(c)
        sizy = w*h
    
        #Choose the largest area, and record box coordinates
        if sizy > area:
            area = sizy
            xx,yy,ww,hh = x,y,w,h
    
    crop_img = thresh_gray[yy:yy+hh, xx:xx+ww]   
    # cv2.imwrite("Finalbox.png", crop_img)
    return crop_img

def BigFigure2(pixs,num,cont_blank):
    # threshold to get just the signature (INVERTED)
    raw = pixs[num,:,:]
    imag = raw.astype(np.uint8)
    retval, thresh_gray = cv2.threshold(imag, 250, maxval=255, \
                                       type=cv2.THRESH_BINARY_INV)
    thresh_gray = 255 - thresh_gray
    label_im, nb_labels = ndimage.label(thresh_gray)

    # Find for each digit, compare their bounding box
    out = ndimage.find_objects(label_im)
    xx, yy, ww, hh, area = 0,0,0,0,0
    for label in out:
    	x = label[0].start
    	y = label[1].start
    	w = label[0].stop - label[0].start
    	h = label[1].stop - label[1].start

    	# size = max(w,h)*max(w,h) + 0.5*w*h
    	size = max(w,h)
    	if size>area:
    		xx = x
    		yy = y
    		ww = w
    		hh = h
    		area = size

    thresh_gray = 255 - thresh_gray
    crop_img = thresh_gray[xx:xx+ww, yy:yy+hh]   
    return crop_img

train_raw, test_raw = [],[]
# train_shape,test_shape = [0,0],[0,0]

for j in range(len(train_images)):
    train_raw.append(BigFigure2(train_images,j,17)) #17 is the result of trial and error, no need to change
    # if train_raw[j].shape[0] > train_shape[0]:
        # train_shape[0] = train_raw[j].shape[0]
    # if train_raw[j].shape[1] > train_shape[1]:
        # train_shape[1] = train_raw[j].shape[1]
    
for j in range(len(test_images)):
    test_raw.append(BigFigure2(test_images,j,17))
    # if test_raw[j].shape[0] > test_shape[0]:
        # test_shape[0] = test_raw[j].shape[0]
    # if test_raw[j].shape[1] > test_shape[1]:
        # test_shape[1] = test_raw[j].shape[1]

def Expand(arr):
    pic = Image.fromarray(arr)
    pic.save("Redundant.png")
    old_im = Image.open('Redundant.png') #IS THERE A WAY TO AVOID GENERATION OF THIS PIC FILE?
    old_size = old_im.size
    shape = max(old_size)
    new_size = [shape,shape]
    if old_size != new_size:
        new_im = Image.new("L", new_size,'white')
        new_im.paste(old_im, (int((new_size[0]-old_size[0])/2),
                      int((new_size[1]-old_size[1])/2)))
    # Resize image data
    IMAGE_SIZE = 28
    image = new_im.resize((IMAGE_SIZE, IMAGE_SIZE), Image.BILINEAR)
    return image

train_procd, test_procd= [],[]
for j in range(len(train_images)):
    train_procd.append(np.array(Expand(train_raw[j])))
    
for j in range(len(test_images)):
    test_procd.append(np.array(Expand(test_raw[j])))

# train_procd and test_procd now should be n*64*64 list of np array
with open('train_labels.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    train_label = [row[1] for row in reader]
train_label = [int(label) for label in train_label[1:]]

# for j in range(len(train_images)):
# 	if j>100:
# 		break
# 	pic = Image.fromarray(train_raw[j])
# 	pic.save(str(j)+'_'+str(train_label[j])+'.jpg')

# raw = train_images[30,:,:]
# imag = raw.astype(np.uint8)
# pic = Image.fromarray(imag)
# pic.save('30.jpg')
# pic = Image.fromarray(train_images[30])
# pic.save('17.jpg')
# pic = Image.fromarray(train_images[48])
# pic.save('48.jpg')
# pic = Image.fromarray(train_images[30])
# pic.save('30.jpg')
# pic = Image.fromarray(train_images[21])
# pic.save('21.jpg')
# dt = np.dtype(float)
# train_procd = train_procd.astype("float32") / 255.0
# test_procd = test_procd.astype("float32") / 255.0

try:
    with open('Xtrain.txt','wb') as x_train_file:
        pickle.dump(train_procd,x_train_file)
    with open('Ytrain.txt','wb') as y_trainfile:
        pickle.dump(train_label,y_trainfile)
    with open('Xtest.txt','wb') as x_test_file:
        pickle.dump(test_procd,x_test_file)
except IOError as err:  
    print('File error: ' + str(err))