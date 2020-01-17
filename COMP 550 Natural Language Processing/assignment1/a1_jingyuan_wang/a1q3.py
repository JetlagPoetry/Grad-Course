import os
import numpy as np
from nltk import word_tokenize
from nltk.corpus import stopwords 
from nltk.stem import WordNetLemmatizer, PorterStemmer
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.naive_bayes import MultinomialNB
from sklearn.svm import SVC
from sklearn.dummy import DummyClassifier
from sklearn.model_selection import train_test_split, cross_val_score, StratifiedKFold, cross_val_predict
from sklearn.metrics import confusion_matrix

def data_input():
	data = []
	# read from 'rt-polaritydata/' folder
	with open('rt-polaritydata/rt-polarity.neg', 'r', encoding='utf-8', errors='ignore') as f:
	    while True:
	        line = f.readline() 
	        if not line:
	            break
	        data.append(line[:-1])
	neg_num = len(data)
	with open('rt-polaritydata/rt-polarity.pos', 'r', encoding='utf-8', errors='ignore') as f:
	    while True:
	        line = f.readline() 
	        if not line:
	            break
	        data.append(line[:-1])
	pos_num = len(data) - neg_num

	negLabel = np.zeros(neg_num)
	posLabel = np.ones(pos_num)
	label = np.concatenate((negLabel,posLabel),axis=0)
	print(label.shape)
	return data, label

def data_processing(input):
	tokens = word_tokenize(input);
	if stpwrd:
		tokens = [token for token in tokens if not token in stop_words] 
	if lemma:
		tokens = [lemmatizer.lemmatize(token) for token in tokens]
	if stem:
		tokens = [stemmer.stem(token) for token in tokens]
	return tokens

if __name__=="__main__":
	#param
	stpwrd = False
	lemma = True
	stem = True
	conf_mat_print = False
	#initialize
	lemmatizer = WordNetLemmatizer()
	stemmer = PorterStemmer() 
	stop_words = set(stopwords.words('english')) 
	count_vect = CountVectorizer(tokenizer=data_processing, min_df=2)

	mnb = MultinomialNB()
	lr = LogisticRegression(tol=0.00001, C=0.8)
	clf = SVC(kernel='linear', C=0.8)
	dummy = DummyClassifier(strategy='uniform')

	data, label = data_input()
	X_data = count_vect.fit_transform(data)
	print(X_data.shape)
	if not conf_mat_print:
		strat_k_fold = StratifiedKFold(n_splits=10, shuffle=True, random_state=2)
		scores = cross_val_score(mnb, X_data, label, cv = strat_k_fold, verbose=1)
		print('Accuracy of classifier with cross validation:{:.8f}'.format(scores.mean()))
	else:
		y_pred = cross_val_predict(lr, X_data, label, cv=10)
		conf_mat = confusion_matrix(label, y_pred)
		print(conf_mat)