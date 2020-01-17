import random
import numpy

def dataInput():
	# Read sequence
	sequence_file = open('sequence.fa','r')
	database = sequence_file.readlines()
	database = database[0].strip()

	# Read probability, store data in a list of dict
	prob_file = open('probability.fa','r')
	prob_file = prob_file.readlines()
	probability = [float(i) for i in prob_file[0].split()]

	nuc_prob = [] #[{'A': 0.003, 'C': 0.99, 'G': 0.003, 'T': 0.003}, {'A': 1.0, 'C': 0.0, 'G': 0.0, 'T': 0.0}]
	for i in range(len(database)):
		nuc_dict = {}
		for nuc in ['A','C','G','T']:
			if nuc == database[i]:
				nuc_dict[nuc] = probability[i]
			else:
				nuc_dict[nuc] = (1-probability[i])/3
		nuc_prob.append(nuc_dict)
	return database, nuc_prob

def generateQuery():
	# Generate a query of length 100 starting from a random position
	start_pos = random.randint(0, len(database)-query_length)
	query = ''
	for i in range(query_length):
		query += numpy.random.choice(list(nuc_prob[start_pos+i].keys()), p = list(nuc_prob[start_pos+i].values()))
	
	# Randomly delete nucleotide
	for i in range(query_length):
		if numpy.random.uniform(0.0, 1.0) < indel_rate/2:
			if i == 0:
				query = query[1:]
			elif i == query_length-1:
				query = query[:-1]
			else:
				query = query[:i] + query[i+1:]
	# Randomly insert nucleotide
	for i in range(len(query)+1):
		if numpy.random.uniform(0.0, 1.0) < indel_rate/2:
			nuc = numpy.random.choice(['A','C','G','T'])
			if i == 0:
				query = nuc + query
			elif i == len(query):
				query = query + nuc
			else:
				query = query[:i] + nuc + query[i:]
	# print(database[start_pos:start_pos+query_length])
	# print(query)
	# print(start_pos, start_pos+query_length-1)
	return start_pos, query

def indexingDatabase():
	# Store indices of each w-mer of the generated database
	# {'ACAGT':[1,12,3452], 'TGCCA':[3414,23432]}
	new_database = ''
	for i in range(len(database)):
		new_database += numpy.random.choice(list(nuc_prob[i].keys()), p = list(nuc_prob[i].values()))
	index_dict = {}
	for i in range(len(new_database)-w+1):
		if new_database[i:i+w] in index_dict.keys():
			index_dict[new_database[i:i+w]].append(i)
		else:
			index_dict[new_database[i:i+w]] = [i]
	return index_dict, new_database

def scanForHits():
	# For each w-mer in query, find HSP
	HSP_dict = {} #{(start,end):score}
	for seed_start_q in range(len(query)-w+1):
		if query[seed_start_q:seed_start_q+w] not in index_dict.keys():
			continue
		for seed_start_d in index_dict[query[seed_start_q:seed_start_q+w]]:
			score, left_index, right_index = findHSP(seed_start_d, seed_start_d+w-1, seed_start_q, seed_start_q+w-1)
			if score > t:
				# print('HSP:', score)
				# print(seed_start_d-left_index, seed_start_d+w+right_index-1)
				# print(query[seed_start_q-left_index:seed_start_q+w+right_index])
				# print(database[seed_start_d-left_index:seed_start_d+w+right_index])  

				# [(ds, de, qs, qe) = score]
				HSP_dict[(seed_start_d-left_index, seed_start_d+w+right_index-1, seed_start_q-left_index, seed_start_q+w+right_index-1)] = score
				
	return HSP_dict

def findHSP(start_d, end_d, start_q, end_q):
	left_extension = True
	right_extension = True
	left_highest_score = 0
	right_highest_score = 0
	left_score = 0
	right_score = 0
	left_highest_index = 0
	right_highest_index = 0
	left_index = 0
	right_index = 0

	
	while left_extension or right_extension:
		if left_extension:
			left_index += 1
			# Stop if exceed the left bound
			if start_d-left_index<0 or start_q-left_index<0:
				left_extension = False
				continue
			# Calculate the new score
			for nuc in nuc_prob[start_d-left_index]:
				if nuc == query[start_q-left_index]:
					left_score += nuc_prob[start_d-left_index][nuc]
				else:
					left_score += -nuc_prob[start_d-left_index][nuc]/3
			# Check if is the highest or lower than delta
			if left_score + delta < left_highest_score:
				left_extension = False
			if left_score >= left_highest_score:
				left_highest_score = left_score
				left_highest_index = left_index

		if right_extension:
			right_index += 1
			# Stop if exceed the right bound
			if end_d+right_index>=len(database) or end_q+right_index>=len(query):
				right_extension = False
				continue

			# Calculate the new score
			for nuc in nuc_prob[end_d+right_index]:
				if nuc == query[end_q+right_index]:
					right_score += nuc_prob[end_d+right_index][nuc]
				else:
					right_score += -nuc_prob[end_d+right_index][nuc]/3

			# Check if is the highest or lower than delta
			if right_score + delta < right_highest_score:
				right_extension = False
			if right_score >= right_highest_score:
				right_highest_score = right_score
				right_highest_index = right_index
	score = left_highest_score+right_highest_score
	return score, left_highest_index, right_highest_index

def gappedExtension():
	align_dict = {}  #[(align_q, align_d) = score]
	# For HSP found
	for hsp in HSP_dict:  #[(ds, de, qs, qe) = score]
		# Perform gapped Extension
		# NW alg to left
		left_align_d = ''
		left_align_q = ''
		left_max_score = 0
		if hsp[2]!=0:
			# Perform only when there are unaligned elements on the left
			if hsp[0] - 4*(hsp[2]+1) < 0:
				score_matrix = numpy.zeros((hsp[0]+1, hsp[2]+1), dtype=float)
				path_matrix = numpy.zeros((hsp[0]+1, hsp[2]+1), dtype=str)
			else:
				score_matrix = numpy.zeros((4*hsp[2]+1, hsp[2]+1), dtype=float)
				path_matrix = numpy.zeros((4*hsp[2]+1, hsp[2]+1), dtype=str)
			# Initialization
			for i in range(len(score_matrix)):
				score_matrix[i][0] = -1*i
				path_matrix[i][0] = '2' # 2 for ^
			for i in range(len(score_matrix[0])):
				score_matrix[0][i] = -1*i
				path_matrix[0][i] = '3' # 3 fo <-
			# Start DP
			for i in range(1, len(score_matrix)):
				for j in range(1, len(score_matrix[0])):
					a = score_matrix[i-1][j-1]
					for nuc in ['A', 'C', 'G', 'T']:
						if nuc == query[hsp[2]-j]:
							a += nuc_prob[hsp[0]-i][nuc]
						else:
							a -= nuc_prob[hsp[0]-i][nuc]
					b = score_matrix[i-1][j] - 1
					c = score_matrix[i][j-1] - 1
					score_matrix[i][j] = max(a,b,c)
					if a == score_matrix[i][j]:
						path_matrix[i][j] = '1'
					elif b == score_matrix[i][j]:
						path_matrix[i][j] = '2'
					else:
						path_matrix[i][j] = '3'

			# Trace back
			last = [i[-1] for i in score_matrix]
			left_max_score = max(last)
			j = len(score_matrix[0])-1
			i = last.index(left_max_score)
			while (i>=0 and j>=0) and not (i==0 and j==0):
				if path_matrix[i][j] == '1':
					left_align_q = left_align_q + query[hsp[2] - j]
					left_align_d = left_align_d + new_database[hsp[0] - i]
					i -= 1
					j -= 1
				elif path_matrix[i][j] == '2':
					left_align_q = left_align_q + '_'
					left_align_d = left_align_d + new_database[hsp[0] - i]
					i -= 1
				else:
					left_align_q = left_align_q + query[hsp[2] - j]
					left_align_d = left_align_d + '_'
					j -= 1


		# NW alg to right
		right_align_d = ''
		right_align_q = ''
		right_max_score = 0
		if len(query)!=hsp[3]+1:
			# Perform only when there are unaligned elements on the right
			if 4*(len(query)-hsp[3]) + hsp[1] < len(new_database):
				score_matrix = numpy.zeros((len(new_database)-hsp[1]-1,len(query)-hsp[3]), dtype=float)
				path_matrix = numpy.zeros((len(new_database)-hsp[1]-1,len(query)-hsp[3]), dtype=str)
			else:
				score_matrix = numpy.zeros((4*(len(query)-hsp[3]),len(query)-hsp[3]), dtype=float)
				path_matrix = numpy.zeros((4*(len(query)-hsp[3]),len(query)-hsp[3]), dtype=str)
			# Initialization
			for i in range(len(score_matrix)):
				score_matrix[i][0] = -1*i
				path_matrix[i][0] = '2' # 2 for ^
			for i in range(len(score_matrix[0])):
				score_matrix[0][i] = -1*i
				path_matrix[0][i] = '3' # 3 fo <-
			# Start DP
			for i in range(1, len(score_matrix)):
				for j in range(1, len(score_matrix[0])):
					a = score_matrix[i-1][j-1]
					for nuc in ['A', 'C', 'G', 'T']:
						if nuc == query[hsp[3]+j]:
							a += nuc_prob[hsp[1]+i][nuc]
						else:
							a -= nuc_prob[hsp[1]+i][nuc]
					b = score_matrix[i-1][j] - 1
					c = score_matrix[i][j-1] - 1
					score_matrix[i][j] = max(a,b,c)
					if a == score_matrix[i][j]:
						path_matrix[i][j] = '1'
					elif b == score_matrix[i][j]:
						path_matrix[i][j] = '2'
					else:
						path_matrix[i][j] = '3'

			# Trace back
			last = [i[-1] for i in score_matrix]
			right_max_score = max(last)
			j = len(score_matrix[0])-1
			i = last.index(right_max_score)
			while (i>=0 and j>=0) and not (i==0 and j==0):
				if path_matrix[i][j] == '1':
					right_align_q = query[hsp[3] + j] + right_align_q
					right_align_d = new_database[hsp[1] + i] + right_align_d
					i -= 1
					j -= 1
				elif path_matrix[i][j] == '2':
					right_align_q = '_'+ right_align_q
					right_align_d = new_database[hsp[1] + i] + right_align_d
					i -= 1
				else:
					right_align_q = query[hsp[3] + j] + right_align_q
					right_align_d = '_' + right_align_d
					j -= 1
		# Best alignment and score for current HSP
		align_d = left_align_d + new_database[hsp[0]:hsp[1]+1] + right_align_d
		align_q = left_align_q + query[hsp[2]:hsp[3]+1] + right_align_q
		score = HSP_dict[hsp] + right_max_score + left_max_score
		print(align_q)
		print(align_d)
		align_dict[(align_q, align_d) = score]
		
if __name__ == "__main__":
	query_length = 100 # length of generated query 
	indel_rate = 0.01 # error rate of insertion or deletion
	t = 10 # threshold for HSP score
	delta = 6
	w = 11
	database, nuc_prob = dataInput()

	# Repeat N times for evaluation
	n = 10
	for i in range(n):
		print('calculating sequence:', i)
		start_pos, query = generateQuery()
		index_dict, new_database = indexingDatabase()
		HSP_dict = scanForHits()
		gappedExtension()

	# print('recall: {:.2f}'.format(recall))
	# print('precision: {:.2f}'.format(precision))