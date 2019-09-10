
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer

import csv
import numpy as np

samplenum=12500#may be smaller to run fast

with open('data/neg.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    rows = []
    for row in reader:
        row=str(row).lower()
        row.replace('<br />', ' ')
        rows.append(row)
negdata=np.array(rows)
negdata=negdata[0:samplenum]
negdata=negdata.ravel()


negY=np.zeros((samplenum,1))


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

data=np.concatenate((negdata,posdata))

vectorizer = CountVectorizer()
X = vectorizer.fit_transform(data)
transformer = TfidfTransformer()
Xdata = transformer.fit_transform(X)


posY = np.ones((samplenum,1))

y=np.vstack((negY,posY))
del negdata,posdata,negY,posY,X,data,reader


print(Xdata.shape)
print(y.shape)

negprob=[]
posprob=[]
rows,cols=Xdata.shape
trainnum=11000#make sure it smaller than sample num
#Xdata=Xdata[:,0:40000]# fewer feature to run faster
rows,cols=Xdata.shape
# #this is a held-out validation
#P(word|class)
for i in range(cols):
    count=1
    count+=Xdata[0:trainnum,i].nonzero()[1].size
    prob=count/trainnum
    negprob.append(prob)

for i in range(cols):
    count=1
    count += Xdata[samplenum:samplenum+trainnum, i].nonzero()[1].size
    prob=count/trainnum
    posprob.append(prob)

correct=0
print("model trained")
#P(class|words)
for test in range (trainnum,samplenum):
    P=1
    for j in range(cols):
        if Xdata[test,j]!=0:
            P *= (negprob[j]/posprob[j])
        else:
            P *= ((1-negprob[j])/(1-posprob[j]))
    if P>1:
        correct+=1

for test in range (samplenum+trainnum,2*samplenum):
    P=1
    for j in range(cols):
        if Xdata[test,j]!=0:
            P *= (negprob[j]/posprob[j])
        else:
            P *= ((1-negprob[j])/(1-posprob[j]))
    if P<1:
        correct+=1

print(correct/2/(samplenum-trainnum))
#output 0.8093

        













