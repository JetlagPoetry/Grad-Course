
# coding: utf-8

# ### Enable AutoCompleting

# In[1]:


#get_ipython().run_line_magic('config', 'IPCompleter.greedy=True # Enable autocomplete')


# In[2]:


import numpy as np
import re
import copy
import math
import time
from nltk import FreqDist
from nltk import ngrams
from nltk.corpus import stopwords 
from nltk.tokenize import word_tokenize
from nltk.sentiment.vader import SentimentIntensityAnalyzer

# In[3]:

import json # we need to use the JSON package to load the data, since the data is stored in JSON format


# In[4]:


with open("proj1_data.json") as fp:
    data = json.load(fp) # data type: list of dictionary
    
# Now the data is loaded.
# It a list of data points, where each datapoint is a dictionary with the following attributes:
# popularity_score : a popularity score for this comment (based on the number of upvotes) (type: float)
# children : the number of replies to this comment (type: int)
# text : the text of this comment (type: string)
# controversiality : a score for how "controversial" this comment is (automatically computed by Reddit)
# is_root : if True, then this comment is a direct reply to a post; if False, this is a direct reply to another comment 

# Example:
# data_point = data[0] # select the first data point in the dataset


# In[5]:


def preprocessing(data):
    Dict = {}
    DATA = copy.deepcopy(data)
    for comment in DATA:
        if 'text' in comment:
            comment["word"] = re.split('\W+', str.lower(comment["text"]))
            for word in comment["word"]:
                if word in Dict:
                    Dict[word] += 1
                else: 
                    Dict[word] = 1 
        #Extract text length
        comment["length"] = len(comment["text"])
        
    # Create a word frequency book from all comments
#     Sorted_Dictionary = sorted(Dict.items(), key=lambda x: x[1], reverse=True) # data type: list
#     # To remove emppty element 
#     for x in Sorted_Dictionary: 
#         if x[0] == '':
#             Sorted_Dictionary.remove(x)
    
#     Trimed_Dictionary = Sorted_Dictionary[:160]
#     # Create a the frequency book for each comment. The Trimed_Dictionary stores a list of tuple thus it should be changed to dictionary 
#     # First, we default the counts
    WordBook = globalWordBook(Dict)
    
    extended_DATA = localWordBook(DATA, WordBook)

    return extended_DATA

            


# In[6]:


def globalWordBook(Dict):
    # Create a word frequency book from all comments
    Sorted_Dictionary = sorted(Dict.items(), key=lambda x: x[1], reverse=True) # data type: list
    # To remove emppty element 
    for item in Sorted_Dictionary: 
        if item[0] == '':
            Sorted_Dictionary.remove(item)
   
    Trimed_Dictionary = Sorted_Dictionary[:160]
    globalWordBook = {}
    for item in Trimed_Dictionary: 
        globalWordBook[item[0]] = 0
    # Create a the frequency book for each comment. The Trimed_Dictionary stores a list of tuple thus it should be changed to dictionary 
    # First, we default the counts
    return globalWordBook


# In[7]:


def localWordBook(DATA, globalWordBook):    
    for comment in DATA:
        Copy_WB = copy.deepcopy(globalWordBook)
        if 'text' in comment:
            for word in comment["word"]:
                if word in Copy_WB:
                    Copy_WB[word] += 1
       
        comment['WordBook'] = Copy_WB
    return DATA


def bigram(DATA):
    bigramWordBook = []
    bigramDict = {}
    for comment in DATA:
        if 'text' in comment:
            # stop_words = set(stopwords.words('english'))
            # clean_text = [w for w in comment["text"] if not w in stop_words]
            # bigrams = ngrams(clean_text, 2)

            bigrams = ngrams(comment["word"], 2)
            bigramDist = FreqDist(bigrams)
            bigramWordBook.append(bigramDist)
            for key in bigramDist.keys():
                if key in bigramDict.keys():
                    bigramDict[key] = bigramDict[key] + bigramDist[key]
                else:
                    bigramDict[key] = bigramDist[key]
    bigramDict = {key:value for key,value in bigramDict.items() if value > 100}
    # print(len(bigramDict))
    for key in bigramDict:
        bigramDict[key] = 0
    for comment, bigrams in zip(DATA, bigramWordBook):
        Copy_BiWB = copy.deepcopy(bigramDict)
        for key in bigrams:
            if key in Copy_BiWB:
                Copy_BiWB[key] += 1
        comment['BigramWordBook'] = Copy_BiWB
        
    return DATA

def sentimentAnalysis(DATA):
	sid = SentimentIntensityAnalyzer()
	for comment in DATA:
		ss = sid.polarity_scores(comment['text'])
		comment['sentiment'] = ss['compound']
	return DATA
# In[8]:

start_time = time.time()
data_preposed = preprocessing(data)
data_preposed = bigram(data_preposed)
data_preposed = sentimentAnalysis(data_preposed)

# In[9]:


# data_point = data_preposed[0]    # test the first comment in the dataset. wordbook shows 0 over all wordcounts
# for info_name, info_value in data_point.items():
#     print(info_name + " : " + str(info_value))


# In[10]:


training_set = data_preposed[0:10000]
validation_set = data_preposed[10000:11001]
test_set = data_preposed[11000:12001]


# In[11]:


class outputStore:
    def __init__(self, X,y):
        self.X = X
        self.y = y


# In[12]:


def featureOutput (data, new_features):
    length = len(data[0]['WordBook'])
    X = []
    y = []
    for comment in data: # interating over dataset
        J = []
        i = 0
        # Assign value of wordbook to array
        for key in comment['WordBook']: # iterating over the wordbook
            J.append(comment['WordBook'][key])     #feature:word count  
        if comment['is_root'] == False:    #feature:is_root
            J.append(0)
        else:
            J.append(1)
        J.append(comment['controversiality'])    #feature:controversiality
        J.append(comment['children'])      #feature:children
        if new_features: 
            J.append(math.log(comment['length']))      #feature: log of length of text
            if comment['is_root'] == False:    #feature: interaction term of length and is_root
                J.append(0)
            else:
                J.append(math.log(comment['length']))
            # for key in comment['BigramWordBook']: # iterating over the bigram wordbook
       	    #     J.append(comment['BigramWordBook'][key])     #feature:top bigram count 
            # J.append(comment['controversiality'] * comment['children'])	#feature: interaction term of children and controversiality
            # J.append(comment['sentiment'])	#feature: sentiment analysis grades

        J.append(1)           #bias
        y.append(comment['popularity_score'])
        X.append(J)

    #Converting to numpy array
    output = outputStore(np.array(X),np.transpose(np.array(y).reshape(1,len(y))))
    return output
    
                         

                 
            
# In[13]:

new_features = True
data_train = featureOutput(training_set, new_features)
data_validate = featureOutput(validation_set, new_features)
data_test = featureOutput(test_set, new_features)
# In[14]:

feature_num = data_train.X.shape[1]

# print(data_train.X.shape)
# print(data_train.y.shape)
# print('Data processing runtime:' + str(time.time() - start_time))


# ### Linear Regression Function

# In[15]:


def linReg(X,y):
    X_transpose = np.transpose(X)
    a = np.linalg.pinv(X_transpose.dot(X))
    b = a.dot(X_transpose)
    c = b.dot(y)
    return c
    
    
ita = 0.000001
beta = 0
epsilon = 0.00001
w = np.zeros((feature_num,1)) 
w = 0.1*w

def graddes(X,y,w, beta, ita, epsilon): # Gradient descent
    
    increment =np.ones((feature_num,1))  
    X_transpose = np.transpose(X)
    a = X_transpose.dot(X)
    d = X_transpose.dot(y)
    
    i = 0
    
    while(np.linalg.norm(increment) > epsilon):
        beta = 0.05*i
        alpha = ita/(1+ beta)
        increment = 2*alpha*(a.dot(w)-d)
        
        w = w - increment
        i = i+1
    return w

	
w_linReg = w

start_time = time.time()
try:
    w_linReg = linReg(data_train.X, data_train.y)
except:
    print("singular matrix error!")
# print('Closed form runtime:' + str(time.time() - start_time))
#print(w_linReg)

start_time = time.time()
w_graddes = graddes(data_train.X, data_train.y, w, beta, ita, epsilon)
# print('Gradient descent runtime:' + str(time.time() - start_time))
#print(w_graddes)


def test(X,y,w):
    y_pred = X.dot(w)
    error = np.linalg.norm(y_pred - y)
    error = error*error
    return(error)

# error_linReg = test(data_train.X, data_train.y, w_linReg)
# error_graddes = test(data_train.X, data_train.y, w_graddes)

# print('Closed form MSE on training set: ' + str(error_linReg/len(data_train.X)))
# print('Gradient descent MSE on training set: ' + str(error_graddes/len(data_train.X)))

error_linReg = test(data_validate.X, data_validate.y, w_linReg)
error_graddes = test(data_validate.X, data_validate.y, w_graddes)

print('Closed form MSE on validation set: ' + str(error_linReg/len(data_validate.X)))
print('Gradient descent MSE on validation set: ' + str(error_graddes/len(data_validate.X)))
	
error_linReg = test(data_test.X, data_test.y, w_linReg)
error_graddes = test(data_test.X, data_test.y, w_graddes)

print('Closed form MSE on test set: ' + str(error_linReg/len(data_test.X)))
print('Gradient descent MSE on test set: ' + str(error_graddes/len(data_test.X)))

