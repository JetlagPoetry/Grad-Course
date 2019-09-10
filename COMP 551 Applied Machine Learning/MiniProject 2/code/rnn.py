from tensorflow.python.keras.preprocessing.text import Tokenizer
from tensorflow.python.keras.preprocessing.sequence import pad_sequences
from keras.layers import Embedding, Dropout, Conv1D, MaxPool1D, GRU, LSTM, Dense
from keras.layers.embeddings import Embedding
from keras.models import Sequential
from sklearn.model_selection import train_test_split
import numpy as np
import csv
import h5py

X = []
y = []

with open('data/neg.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    X = [row[0] for row in reader]
y = np.zeros(len(X))

with open('data/pos.csv','r') as csvfile:
	reader = csv.reader(csvfile)
	X_temp = [row[0] for row in reader]
X = np.concatenate((X, X_temp), axis=0)
y_temp = np.ones(len(X_temp))
y = np.concatenate((y, y_temp), axis=0)

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=0, shuffle=True)
max_features = 5000
tokenizer_obj = Tokenizer(lower=True, split=' ', num_words=max_features)
tokenizer_obj.fit_on_texts(X)

# max_length = max([len(s.split()) for s in X])
max_length = 500

X_train_tokens = tokenizer_obj.texts_to_sequences(X_train)
X_test_tokens = tokenizer_obj.texts_to_sequences(X_test)
X_train_pad = pad_sequences(X_train_tokens, maxlen=max_length)
X_test_pad = pad_sequences(X_test_tokens, maxlen=max_length)

batch_size = 64
model = Sequential()
model.add(Embedding(input_dim = max_features, 
                            output_dim = 128,
                            input_length = max_length))

model.add(Dropout(0.4))
model.add(Conv1D(filters = 32, kernel_size = 3, padding = 'same', activation = 'relu'))
model.add(MaxPool1D(pool_size = 2))
model.add(LSTM(100))
# model.add(GRU(100))	
model.add(Dropout(0.2))
model.add(Dense(1, activation = 'sigmoid'))             
model.compile(loss = 'binary_crossentropy', optimizer = 'adam', metrics = ['accuracy'])

model.fit(X_train_pad, y_train, batch_size = batch_size, epochs = 5,  validation_data=(X_test_pad, y_test), verbose=1)
score, acc = model.evaluate(X_test_pad, y_test, verbose = 2, batch_size = batch_size)
print('Score:%.4f'%(score))
print('Accuracy:%.4f'%(acc))