import glob
import os
import string
import csv

import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.preprocessing import Normalizer
from sklearn.pipeline import Pipeline
from nbsvm import NBSVM


def load_imdb():
    print("Vectorizing Training Text")

    train_pos = glob.glob(os.path.join('aclImdb', 'train', 'pos', '*.txt'))
    train_neg = glob.glob(os.path.join('aclImdb', 'train', 'neg', '*.txt'))

    token_pattern = r'\w+|[%s]' % string.punctuation

    vectorizer = CountVectorizer('filename', ngram_range=(1, 3),
                                 token_pattern=token_pattern,
                                 binary=True)
    X_train = vectorizer.fit_transform(train_pos+train_neg)
    y_train = np.array([1]*len(train_pos)+[0]*len(train_neg))

    print("Vocabulary Size: %s" % len(vectorizer.vocabulary_))
    print("Vectorizing Testing Text")

    test_pos = glob.glob(os.path.join('aclImdb', 'test', 'pos', '*.txt'))
    test_neg = glob.glob(os.path.join('aclImdb', 'test', 'neg', '*.txt'))

    X_test = vectorizer.transform(test_pos + test_neg)
    y_test = np.array([1]*len(test_pos)+[0]*len(test_neg))

    return X_train, y_train, X_test, y_test

def test_data():
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

	X_test = []
	files = os.listdir('test/')
	files.sort(key=lambda x:int(x[:-4]))
	for file in files:
	    f = open('test/'+file, 'r', errors='ignore')
	    line = f.readline()
	    X_test.append(line)

	token_pattern = r'\w+|[%s]' % string.punctuation

	pclf = Pipeline([
	    ('vect', CountVectorizer(ngram_range=(1, 2), token_pattern=token_pattern, binary=True, lowercase=True)),
	    ('tfidf', TfidfTransformer()),
	    ('norm', Normalizer()),
	])
	# X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=0, shuffle=True)
	X = pclf.fit_transform(X)
	X_test = pclf.transform(X_test)
	X_train = X
	y_train = y
	return X_train, y_train, X_test

def val_data():
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

	token_pattern = r'\w+|[%s]' % string.punctuation

	pclf = Pipeline([
	    ('vect', CountVectorizer(ngram_range=(1, 2), token_pattern=token_pattern, binary=True, lowercase=True)),
	    ('tfidf', TfidfTransformer()),
	    ('norm', Normalizer()),
	])
	X = pclf.fit_transform(X)
	X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=0, shuffle=True)
	return X_train, y_train, X_test, y_test

def val():
	#Not using cross validation here but held-out one
	X_train, y_train, X_test, y_test= val_data()
	print("Fitting Model")
	mnbsvm = NBSVM()
	mnbsvm.fit(X_train, y_train)
	print('Test Accuracy: %s' % mnbsvm.score(X_test, y_test))

def test():
	X_train, y_train, X_test = test_data()
	mnbsvm = NBSVM()
	mnbsvm.fit(X_train, y_train)
	y_test = mnbsvm.predict(X_test)
	with open("result.csv", 'w+', newline='') as csvfile:
		writer = csv.writer(csvfile)
		writer.writerow(['Id', 'Category'])
		i = 0
		for label in y_test:
			writer.writerow([i, int(y_test[i])])
			i += 1
def main():
	# val()
	test()

if __name__ == '__main__':
    main()
