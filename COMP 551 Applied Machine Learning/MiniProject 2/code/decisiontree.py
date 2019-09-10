from sklearn import feature_extraction
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
import sklearn.neural_network as sk_nn
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.svm import SVC
from sklearn.model_selection import KFold,StratifiedKFold
from sklearn.svm import LinearSVC
import csv
import numpy as np
import os


from sklearn.tree import DecisionTreeClassifier

samplenum=12500#max 12500
#stopworddic = set(stopwords.words('english'))


with open('data/neg.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    rows=[]
    for row in reader:
        row=str(row).lower()
        row.replace('<br />', ' ')
        rows.append(row)

negdata=np.array(rows)
negdata=negdata[0:samplenum]
negdata=negdata.ravel()


negY=np.zeros((samplenum,1)).astype(int)

with open('data/pos.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    rows=[]
    for row in reader:
        row=str(row).lower()
        row.replace('<br />', ' ')
        rows.append(row)
posdata = np.array(rows)
posdata=posdata[0:samplenum]
posdata = posdata.ravel()

#Read in the test data
X_test = []
files = os.listdir('test/')
files.sort(key=lambda x:int(x[:-4]))
for file in files:
    f = open('test/'+file, 'r',errors='ignore')
    line = f.readline()
    line=line.lower()
    line.replace('<br />', ' ')
    X_test.append(line)
Xtest2=np.array(X_test)
Xtest2 =Xtest2.ravel()


data=np.concatenate((negdata,posdata))

vectorizer = CountVectorizer()
vec_trans = vectorizer.fit(data)

X = vec_trans.transform(data)
test= vec_trans.transform(Xtest2)

transformer = TfidfTransformer()
tfidf = transformer.fit_transform(X)

transformer = TfidfTransformer()
test = transformer.fit_transform(test)
print(tfidf.shape)



#test=tfidf[samplenum*2:samplenum*2+25000]
Xdata=tfidf[0:samplenum*2]
posY = np.ones((samplenum,1)).astype(int)
y=np.vstack((negY,posY)).ravel()
del negdata,posdata,negY,posY,X,tfidf,data,reader

print(Xdata.shape)
print(y.shape)


tree = DecisionTreeClassifier(criterion='entropy', max_depth=15, random_state=0)

strat_k_fold = StratifiedKFold(n_splits=5, shuffle=True, random_state=2)
scores = cross_val_score(tree, Xdata, y, cv = strat_k_fold, scoring='f1_macro', verbose=1)
print('Accuracy of svm classifier with cross validation:{:.4f}'.format(scores.mean()))

#print("Model testing finished..")