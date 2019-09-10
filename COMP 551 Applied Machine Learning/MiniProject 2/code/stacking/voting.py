import csv
import os
import numpy as np

#Voting 
with open('cnn.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    a = [row[1] for row in reader][1:]
a = [int(row) for row in a]

with open('tfidf-regression.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    b = [row[1] for row in reader][1:]
b = [int(row) for row in b]

with open('tfidf-svm.csv','r') as csvfile:
    reader = csv.reader(csvfile)
    c = [row[1] for row in reader][1:]
c = [int(row) for row in c]

result = np.vstack((b,c))
result = np.vstack((result,a))
result = np.sum(result,axis=0)

with open("result.csv",'w',newline='') as csvfile:
	writer = csv.writer(csvfile)
	writer.writerow(['Id','Category'])
	i = 0
	for label in result:
		if label>1.5:
			writer.writerow([i,1])
		else:
			writer.writerow([i,0])
		i+=1
