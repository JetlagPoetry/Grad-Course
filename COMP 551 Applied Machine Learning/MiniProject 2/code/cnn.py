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
epochs = 4

# (x_train, y_train), (x_test, y_test) = imdb.load_data(num_words=max_features)
with open('data/neg.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    X = [row[0] for row in reader]
    # X = X[:1000]
y = np.zeros(len(X))

with open('data/pos.csv','r') as csvfile:
	reader = csv.reader(csvfile)
	X_temp = [row[0] for row in reader]
	# X_temp = X_temp[:1000]
X = np.concatenate((X, X_temp), axis=0)
y_temp = np.ones(len(X_temp))
y = np.concatenate((y, y_temp), axis=0)
x_train, x_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=0, shuffle=True)

tokenizer_obj = Tokenizer(lower=True, split=' ', num_words=max_features)#num_words=5000, 
tokenizer_obj.fit_on_texts(X)
x_train = tokenizer_obj.texts_to_sequences(x_train)
x_test = tokenizer_obj.texts_to_sequences(x_test)

# Pad sequences
x_train = sequence.pad_sequences(x_train, maxlen=maxlen)
x_test = sequence.pad_sequences(x_test, maxlen=maxlen)

print('Train data size:', x_train.shape)
print('Test data size:', x_test.shape)

model = Sequential()

# we start off with an efficient embedding layer which maps
# our vocab indices into embedding_dims dimensions
model.add(Embedding(max_features, 
                    embedding_size, 
                    input_length=maxlen))
model.add(Dropout(0.4))


model.add(Conv1D(filters,
                kernel_size,
                padding='valid',
                activation='relu',
                strides=1))
model.add(GlobalMaxPooling1D())

# We add a vanilla hidden layer:
model.add(Dense(hidden_dims))
model.add(Dropout(0.25))
model.add(Activation('relu'))

# We project onto a single unit output layer, and squash it with a sigmoid:
model.add(Dense(1))
model.add(Activation('sigmoid'))

model.compile(loss='binary_crossentropy',
              optimizer='adam',
              metrics=['accuracy'])

model.summary()

# Train the model
model.fit(x_train, y_train,
         batch_size=batch_size,
         epochs=epochs,
         validation_data=(x_test, y_test),
         verbose=1)

# Evaluate model
score, acc = model.evaluate(x_test, y_test, batch_size=batch_size)
print('Score:%.4f'%(score))
print('Accuracy:%.4f'%(acc))