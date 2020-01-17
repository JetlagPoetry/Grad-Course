import numpy as np
import math
import sys
###usage: python align.py short 1 -1 1
file_name = sys.argv[1] + ".fa"
i = 0
with open(file_name) as f:
	while True:
		line = f.readline() 
		if len(line)>0 and line[0]==">":
			line = f.readline()
			if i==0:
				S = line[:-1]
				i+=1
			else:
				T = line
		if not line:
			break
m = len(S)
n = len(T)
print(m)
print(n)
match = float(sys.argv[2])
mismatch = float(sys.argv[3])
b = float(sys.argv[4])#gap penalty
M = np.zeros((m+1, n+1))#Given S(i) aligned to T(j)
IX = np.zeros((m+1, n+1))#Given S(i) aligned to a gap
IY = np.zeros((m+1, n+1))#Given T(j) aligned to a gap
STATE_M = np.zeros((m+1, n+1))
STATE_IX = np.zeros((m+1, n+1))
STATE_IY = np.zeros((m+1, n+1))
for i in range(m+1):
	M[i][0] = -b*i
	IX[i][0] = -b*i
	IY[i][0] = -math.inf
	STATE_IX[i][0] = 2
	STATE_M[i][0] = 2
	STATE_IY[i][0] = 2
for j in range(n+1):
	M[0][j] = -b*j
	IX[0][j] = -math.inf
	IY[0][j] = -b*j
	STATE_IX[0][j] = 3
	STATE_M[0][j] = 3
	STATE_IY[0][j] = 3
for i in range(1,m+1):
	for j in range(1,n+1):
		print(i,j)
		s = mismatch
		if S[i-1]==T[j-1]:
			s = match
		M[i][j] = max(M[i-1][j-1]+s, IX[i-1][j-1]+s, IY[i-1][j-1]+s)
		if M[i-1][j-1]+s == M[i][j]:
			STATE_M[i][j] = 1#M<-M
		elif IX[i-1][j-1]+s == M[i][j]:
			STATE_M[i][j] = 2#M<-IX
		elif IY[i-1][j-1]+s == M[i][j]:
			STATE_M[i][j] = 3#M<-IY

		IX[i][j] = max(M[i-1][j]-b, IY[i-1][j]-b)
		if M[i-1][j]-b == IX[i][j]:
			STATE_IX[i][j] = 1#IX<-M
		elif IY[i-1][j]-b == IX[i][j]:
			STATE_IX[i][j] = 3#IX<-IY

		IY[i][j] = max(M[i][j-1]-b, IX[i][j-1]-b)
		if M[i][j-1]-b == IY[i][j]:
			STATE_IY[i][j] = 1#IY<-M
		elif IX[i][j-1]-b == IY[i][j]:
			STATE_IY[i][j] = 2#IY<-IX

i = m
j = n
current_state = 1#State.M
max_score = max(M[m][n],IX[m][n],IY[m][n])
if IY[m][n]==max_score:
	current_state = 3
elif IX[m][n]==max_score:
	current_state = 2


S_prime=[]
T_prime=[]
print("begin traceback")
while i>0 and j>0:
	# print("index",i,j,current_state)
	if current_state==1:#State.M:
		S_prime.insert(0,S[i-1])
		T_prime.insert(0,T[j-1])
		current_state = STATE_M[i][j]
		i-=1
		j-=1
	elif current_state==2:
		S_prime.insert(0,S[i-1])
		T_prime.insert(0,'_')
		current_state = STATE_IX[i][j]
		i-=1
	elif current_state==3:
		S_prime.insert(0,'_')
		T_prime.insert(0,T[j-1])
		current_state = STATE_IY[i][j]
		j-=1
	
if i==0:
	while not j==0:
		S_prime.insert(0,'_')
		T_prime.insert(0,T[j-1])
		j-=1
if j==0:
	while not i==0:
		S_prime.insert(0,S[i-1])
		T_prime.insert(0,'_')
		i-=1
print("S:",*S_prime,sep="")
print("T:",*T_prime,sep="")
print("Score:",max_score)