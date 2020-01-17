import argparse
import re
import nltk
from nltk.tag import hmm
from nltk.probability import LaplaceProbDist, MLEProbDist, ConditionalFreqDist, ConditionalProbDist, FreqDist

def argument_parse():
	ap = argparse.ArgumentParser()
	ap.add_argument('-laplace', action='store_true', help='whether to use laplace or not')
	ap.add_argument('-lm', action='store_true', help='whether to use improved language modelling or not')
	ap.add_argument('cipher_folder')
	args = vars(ap.parse_args())
	folder = args['cipher_folder']
	useLaplace = args['laplace']
	useLm = args['lm']
	print('Deciphering folder:', folder)
	if useLaplace: 
		print('Using laplace smoothing')
	else:
		print('Not using laplace smoothing')
	if useLm: 
		print('Using improved language modelling')
	else:
		print('Not using improved language modelling')
	return folder, useLaplace, useLm

def feature_input(folder):
	symbols = ""
	data = []
	with open('a2data/' + folder + '/train_cipher.txt', 'r', encoding='utf-8', errors='ignore') as f:
		while True:
			line = f.readline() 
			if not line:
				break
			data.append(list(line[:-1]))
			symbols += line[:-1]
	train_data = list(data)
	train_transition = ""
	data = []
	with open('a2data/' + folder + '/train_plain.txt', 'r', encoding='utf-8', errors='ignore') as f:
		while True:
			line = f.readline() 
			if not line:
				break
			data.append(list(line[:-1]))
			train_transition += line[:-1]+' '
	train_tag = list(data)
	train_transition = list(train_transition)
	train_tagged = []
	train_output = []
	for i in range(len(train_data)):
		train_tagged.append(list(zip(train_data[i], train_tag[i])))
		train_output.append(list(zip(train_tag[i], train_data[i])))

	data = []
	with open('a2data/' + folder + '/test_cipher.txt', 'r', encoding='utf-8', errors='ignore') as f:
		while True:
			line = f.readline() 
			if not line:
				break
			data.append(list(line[:-1]))
	test_data = list(data)
	data = []
	with open('a2data/' + folder + '/test_plain.txt', 'r', encoding='utf-8', errors='ignore') as f:
		while True:
			line = f.readline() 
			if not line:
				break
			data.append(list(line[:-1]))
	test_tag = list(data)
	test_tagged = []
	for i in range(len(test_data)):
		test_tagged.append(list(zip(test_data[i], test_tag[i])))

	symbols = set(symbols)
	return train_tagged, test_tagged, test_data, symbols, train_output, train_transition

def trainModel(train_sent, laplace, symbols):
	if laplace:
		estimator = LaplaceProbDist
	else:
		estimator = MLEProbDist

	trainer = hmm.HiddenMarkovModelTrainer(symbols=symbols)
	model = trainer.train(labeled_sequences=train_sent, estimator=estimator)
	return model

def trainModelLM(laplace, symbols, train_output, train_transition):
	extra_set = []
	for i in symbols:
		for j in symbols:
			extra_set.append((i,j))

	transition = suppleText(train_transition);
	initial=[]
	output=[]
	for i in range(len(train_output)):
		initial.append(train_output[i][0][1])
		for j in range(len(train_output[i])):
			output.append(train_output[i][j])

	if laplace:
		transition += extra_set
		initial += symbols
		output += extra_set
	transition_cfd = ConditionalFreqDist(transition)
	transition_cqd = ConditionalProbDist(transition_cfd, MLEProbDist)
	inital_cfd = FreqDist(initial)
	initial_cqd = MLEProbDist(inital_cfd)
	output_cfd = ConditionalFreqDist(output)
	output_cqd = ConditionalProbDist(output_cfd, MLEProbDist)
	model = hmm.HiddenMarkovModelTagger(symbols=symbols, states=symbols, transitions=transition_cqd, outputs=output_cqd, priors =initial_cqd)
	return model

def suppleText(train_transition):
	data=""
	data = nltk.corpus.gutenberg.raw()#open(nltk.corpus.gutenberg.words('austen-persuasion.txt'),"r").read().split().lower()
	data = data.split('\n')
	new_data=""
	for sentence in data:
		sentence = sentence.strip().lower()
		new_data += sentence
	new_data = re.sub('[^a-z.,\s]','',new_data)
	# print(new_data)
	supple_text = list(new_data)
	supple_text += train_transition
	bigrams_chars = nltk.bigrams(supple_text)
	return list(bigrams_chars)

def test(model, test_sent, test_raw):

	print()
	print('deciphered test set:')
	test_decipher = []
	for i in range(len(test_raw)):
		temp = list(zip(*model.tag(test_raw[i])))
		sentence = ''.join(temp[1])
		test_decipher.append(sentence)
		print(sentence)
	
	print()
	model.test(test_sent, verbose=False)

_folder, _laplace, _lm = argument_parse()
_train_sent, _test_sent, _test_raw , _symbols, _train_output, _train_transition= feature_input(_folder)
if not _lm:
	_model = trainModel(_train_sent, _laplace, _symbols)
	test(_model, _test_sent, _test_raw)
else:
	_model = trainModelLM(_laplace, _symbols, _train_output, _train_transition)
	test(_model, _test_sent, _test_raw)
