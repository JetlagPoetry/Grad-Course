from sklearn import feature_extraction
from sklearn.linear_model import LogisticRegression
from sklearn.svm import SVC
from sklearn.externals import joblib
from sklearn.model_selection import train_test_split, cross_val_score, StratifiedKFold
from sklearn.preprocessing import StandardScaler
from nltk import word_tokenize
from sklearn import metrics

import csv
import math
import numpy as np
import nltk
import os

def lexicon_processing():
	sentiment_lexicon = []
	with open('data/negative-words.txt', 'r') as f:
	    while True:
	        line = f.readline() 
	        if not line:
	            break
	        sentiment_lexicon.append(line[:-1])
	with open('data/positive-words.txt', 'r') as f:
	    while True:
	        line = f.readline() 
	        if not line:
	            break
	        sentiment_lexicon.append(line[:-1])
	# try:
	# 	with open('SentimentLexicon.txt','wb') as sentiment_file:
	# 		joblib.dump(sentiment_lexicon,sentiment_file)
	# except IOError as err:  
	# 	print('File error: ' + str(err))  
	return sentiment_lexicon

def feature_extraction(sentiment_lexicon):
	# try:
	# 	with open('SentimentLexicon.txt', 'rb') as sentiment_file:
	# 		sentiment_lexicon = joblib.load(sentiment_file)
	# except IOError as err:
	# 	print('File error: ' + str(err))
	X = []
	y = []
	with open('data/neg.csv','r') as csvfile:
	    reader = csv.reader(csvfile)
	    rows = [row for row in reader]
	sentiment_neg = sentiment_extraction(rows, sentiment_lexicon)
	X = sentiment_neg
	y = np.zeros(len(X))
	with open('data/pos.csv','r') as csvfile:
	    reader = csv.reader(csvfile)
	    rows = [row for row in reader]
	sentiment_pos = sentiment_extraction(rows, sentiment_lexicon)
	X = np.concatenate((X, sentiment_pos), axis=0)
	y_temp = np.ones(len(sentiment_pos))
	y = np.concatenate((y, y_temp), axis=0)
	
	text_test = []
	files = os.listdir('test/')
	files.sort(key=lambda x:int(x[:-4]))
	for file in files:
		f = open('test/'+file, 'r', errors='ignore')
		line = f.readline()
		text_test.append(line)

	# try:
	# 	with open('Xdata.txt','wb') as X_file:
	# 		joblib.dump(X,X_file)
	# 	with open('Ydata.txt','wb') as y_file:
	# 		joblib.dump(y,y_file)
	# 	with open('testdata.txt','wb') as test_file:
	# 		joblib.dump(X_test,test_file)
	# except IOError as err:  
	# 	print('File error: ' + str(err))  
	return X, y

def sentiment_extraction(data, sentiment_lexicon):
	sentiment_X = []
	i = 0
	for review in data:
		print(i)
		i+=1
		# if i>20:
		# 	break
		sentiment = [0] * len(sentiment_lexicon)
		tokens = word_tokenize(review[0])
		for token in tokens:
			if token in sentiment_lexicon:
				index = sentiment_lexicon.index(token)
				sentiment[index] += 1
		sentiment = [math.sqrt(s) for s in sentiment] #squared occurence
		sentiment_X.append(sentiment)
	return sentiment_X

def logistic_regression(X,y):
	# print(X.shape)
	# print(y.shape)
	# X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=0, shuffle=True)
	logreg = LogisticRegression()
	# logreg.fit(X_train, y_train.ravel())
	# print('Accuracy of logistic regression classifier on test set: {:.4f}'.format(logreg.score(X_test, y_test)))
	strat_k_fold = StratifiedKFold(n_splits=5, shuffle=True, random_state=2)
	scores = cross_val_score(logreg, X, y, cv=strat_k_fold, scoring='f1_macro')
	print('Accuracy of logistic regression classifier with cross validation:{:.4f}'.format(scores.mean()))

def logistic_regression_test(X, y):
	X_test = []
	for review in text_test:
		sentiment = [0] * len(sentiment_lexicon)
		tokens = word_tokenize(review)
		for token in tokens:
			if token in sentiment_lexicon:
				index = sentiment_lexicon.index(token)
				sentiment[index] += 1
		X_test.append(sentiment)

	#Shuffle training data
	y = y[:,np.newaxis]
	dataset = np.concatenate((y,X), axis = 1)
	np.random.shuffle(dataset)
	X = dataset[:,1:]
	y = dataset[:,0]

	logreg = LogisticRegression()
	logreg.fit(X, y.ravel())
	y_test = logreg.predict(X_test)
	print(y_test)
	#Ouput result to csv file
	with open("result.csv",'w+',newline='') as csvfile:
		writer = csv.writer(csvfile)
		writer.writerow(['Id','Category'])
		i = 0
		for label in y_test:
			writer.writerow([i,int(y_test[i])])
			i+=1

sentiment_lexicon = lexicon_processing()
X, y = feature_extraction(sentiment_lexicon)

# try:
#     with open('Xdata.txt', 'rb') as x_file:
#         X = joblib.load(x_file)
#     with open('Ydata.txt', 'rb') as y_file:
#         y = joblib.load(y_file)
#     with open('testdata.txt', 'rb') as test_file:
#     	test = joblib.load(test_file)
# except IOError as err:
#     print('File error: ' + str(err))

logistic_regression(X, y)
# logistic_regression_test(X, y)
