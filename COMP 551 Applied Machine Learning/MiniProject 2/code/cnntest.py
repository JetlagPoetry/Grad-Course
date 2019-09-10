import itertools
import numpy as np
from sklearn.metrics import confusion_matrix, classification_report, f1_score
from tensorflow.python.keras.preprocessing.text import Tokenizer
from keras.preprocessing import sequence
from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation
from keras.layers import Embedding, SpatialDropout1D
from keras.layers import LSTM
from keras.layers import Conv1D, GlobalMaxPooling1D
from keras.layers import Flatten
from keras.datasets import imdb
from keras.utils import plot_model
from keras.utils.vis_utils import model_to_dot
from sklearn.model_selection import train_test_split
from keras import regularizers
import csv
import os

# Embedding
embedding_size = 200#50
max_features = 5000
maxlen = 400

# Convolution
kernel_size = 3
pool_size = 4
filters = 250

# Dense
hidden_dims = 250

# Training
batch_size = 64
epochs = 3

with open('data/neg.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    X_train = [row[0] for row in reader]
y_train = np.zeros(len(X_train))

with open('data/pos.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    X_temp = [row[0] for row in reader]
X_train = np.concatenate((X_train, X_temp), axis=0)
y_temp = np.ones(len(X_temp))
y_train = np.concatenate((y_train, y_temp), axis=0)

X_test = []
files = os.listdir('test/')
files.sort(key=lambda x:int(x[:-4]))
for file in files:
    f = open('test/'+file, 'r', errors='ignore')
    line = f.readline()
    X_test.append(line)

X = np.concatenate((X_train, X_test), axis=0)
X_temp, X_val, y_temp, y_val = train_test_split(X_train, y_train, test_size=0.3, random_state=0, shuffle=True)

tokenizer_obj = Tokenizer(lower=True, split=' ', num_words=max_features)
tokenizer_obj.fit_on_texts(X)
X_train = tokenizer_obj.texts_to_sequences(X_train)
X_test = tokenizer_obj.texts_to_sequences(X_test)
X_val = tokenizer_obj.texts_to_sequences(X_val)

X_train = sequence.pad_sequences(X_train, maxlen=maxlen)
X_test = sequence.pad_sequences(X_test, maxlen=maxlen)
X_val = sequence.pad_sequences(X_val, maxlen=maxlen)

model = Sequential()
model.add(Embedding(max_features,  embedding_size, input_length=maxlen))
model.add(Dropout(0.4))
model.add(Conv1D(filters, kernel_size, padding='valid', activation='relu', strides=1))
model.add(GlobalMaxPooling1D())
model.add(Dense(hidden_dims))
model.add(Dropout(0.25))
model.add(Activation('relu'))
model.add(Dense(1))
model.add(Activation('sigmoid'))
model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])
model.summary()

model.fit(X_train, y_train, batch_size=batch_size, epochs=epochs, verbose=1)

y_test = model.predict_classes(X_test, batch_size=batch_size)
#Ouput result to csv file
with open("result.csv",'w',newline='') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['Id','Category'])
    i = 0
    for label in y_test:
        writer.writerow([i,int(y_test[i])])
        i+=1
