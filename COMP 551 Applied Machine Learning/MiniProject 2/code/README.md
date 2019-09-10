In the validation tabel in our writeup file, we mentioned 11 models in total and here lists in order how and where each model is implemented.
Most model is implemented in seperate files with Python3. 
Notable external libraries includes Scikit learn, nltk, Keras etc.
Other needed folders are: 'test' with test data, 'train' with train data, 'nbsvm' with nbsvm model, 'data' with lexicon input and csv files for quicker processing

0. CSV writing script
For shorter training data processing time, we firstly converted txt data into two csv file.
code: csvwrite.py

1. BNB
code: BNB.py
input: data\neg.csv, data\pos.csv
remark: Need real long runtime (approximately over half an hour)

2. Logistic Regression + tf-idf
code: tfidf-lrsvm.py
input: data\neg.csv, data\pos.csv, test
remark: Need to change statement in line 111 to pclf = pclf1 if writing its test result
	File reading in this script has mac and windows version.

3. Logistic Regression + bigram tf-idf
code: tfidf-lrsvm.py
input: data\neg.csv, data\pos.csv, test
remark: Need to change statement in line 111 to pclf = pclf2 if writing its test result

4. Logistic Regression + sentiment lexicon occurrence
code: sentiment-lr.py
input: data\neg.csv, data\pos.csv, test, data/negative-words.txt, data/positive-words.txt
remark: Data processing runtime is long, need to run logistic_regression_test(X, y) in line 152 to choose result writing mode

5. Decision Tree + tfidf
code: decisiontree.py
input: data\neg.csv, data\pos.csv, test

6. LinearSVC + tf-idf
code: tfidf-lrsvm.py
input: data\neg.csv, data\pos.csv, test
remark: Need to change statement in line 111 to pclf = pclf3 if writing its test result

7. LinearSVC + bigram tf-idf
code: tfidf-lrsvm.py
input: data\neg.csv, data\pos.csv, test
remark: Need to change statement in line 111 to pclf = pclf4 if writing its test result

8. NBSVM + bigram tf-idf
code: nbsvm.py
input: data\neg.csv, data\pos.csv, test

9. CNN
code: cnn.py (result output part is in file cnntest.py)
input: data\neg.csv, data\pos.csv, test

10.RNN
code: rnn.py
input:data\neg.csv, data\pos.csv

11.Stacking approach
code: stacking\voting.py
remark: Instead of combine all classifiers in one script, we performs a majority voting towards output of each model.