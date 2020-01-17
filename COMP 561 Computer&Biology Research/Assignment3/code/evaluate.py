def read_anno():
	#open and read the annotation file
	anno_file = open('Vibrio_vulnificus.ASM74310v1.37.gff3','r')
	anno_data = anno_file.readlines()

	anno_dict = {}
	seq_name_list = []
	for i in range(1,389):
		line = anno_data[i].strip().split()
		seq_name_list.append(line[1])
		anno_dict[line[1]] = []

	#dict store all anno data, eg.{'contig_1':[(s,e),(s,e),(s,e),(s,e)],'contig_2':[(s,e),(s,e),(s,e)]}
	line_index = 395
	curr_anno = ''
	while line_index < len(anno_data):
		if anno_data[line_index].startswith('###') and curr_anno!='':
			anno = curr_anno.split()
			if anno[6]=='+':
				anno_dict[anno[0]].append((anno[3],anno[4]))
			curr_anno = ''
		else:
			curr_anno = curr_anno + anno_data[line_index] + '\n'
		line_index += 1
	return anno_dict, seq_name_list

def read_my_anno():
	anno_file = open('result.gff3','r')
	anno_data = anno_file.readlines()
	gene_total_count = 0
	gene_correct_count = 0
	my_anno_dict = {}
	for name in seq_name_list:
		my_anno_dict[name] = []
	for line in anno_data:
		if line.startswith('###'):
			continue
		gene_total_count += 1
		anno = line.strip().split()
		seq_name = anno[0]
		my_anno_dict[seq_name].append((anno[3], anno[4]))
		if (anno[3], anno[4]) in anno_dict[seq_name]:
			gene_correct_count += 1
	print('accuracy', gene_correct_count/gene_total_count)
	return my_anno_dict

def report_anno():
	perfect_anno_dict = {}
	start_anno_dict = {}
	end_anno_dict = {}
	neither_anno_dict = {}
	for name in seq_name_list:
		perfect_anno_dict[name] = []
		start_anno_dict[name] = []
		end_anno_dict[name] = []
		neither_anno_dict[name] = []

	for seq_name in anno_dict:
		for gene in anno_dict[seq_name]:
			#1.perfectly match
			if gene in my_anno_dict[seq_name]:
				perfect_anno_dict[seq_name].append(gene)
			elif gene[0] in [i[0] for i in my_anno_dict[seq_name]]:
				start_anno_dict[seq_name].append(gene)
			elif gene[1] in [i[1] for i in my_anno_dict[seq_name]]:
				end_anno_dict[seq_name].append(gene)
			else:
				neither_anno_dict[seq_name].append(gene)

	result_file = open('fraction_anno.gff3', 'w')
	result_file.write('###  Perfectly match both ends of one of my predicted genes\n')
	for seq_name in anno_dict:
		for gene in perfect_anno_dict[seq_name]:
			result_file.write(seq_name+'\t ena\t CDS \t {}\t {}\t . \t +\t 0\t .\n'.format(gene[0], gene[1]))
	result_file.write('###  Match the start but not the end of a predicted gene\n')
	for seq_name in anno_dict:
		for gene in start_anno_dict[seq_name]:
			result_file.write(seq_name+'\t ena\t CDS \t {}\t {}\t . \t +\t 0\t .\n'.format(gene[0], gene[1]))
	result_file.write('###  Match the end but not the start of a predicted gene\n')
	for seq_name in anno_dict:
		for gene in end_anno_dict[seq_name]:
			result_file.write(seq_name+'\t ena\t CDS \t {}\t {}\t . \t +\t 0\t .\n'.format(gene[0], gene[1]))
	result_file.write('###  Do not match neither the start not the end of a predicted gene\n')
	for seq_name in anno_dict:
		for gene in neither_anno_dict[seq_name]:
			result_file.write(seq_name+'\t ena\t CDS \t {}\t {}\t . \t +\t 0\t .\n'.format(gene[0], gene[1]))
	result_file.close()

def report_my_anno():
	perfect_anno_dict = {}
	start_anno_dict = {}
	end_anno_dict = {}
	neither_anno_dict = {}
	for name in seq_name_list:
		perfect_anno_dict[name] = []
		start_anno_dict[name] = []
		end_anno_dict[name] = []
		neither_anno_dict[name] = []

	for seq_name in my_anno_dict:
		for gene in my_anno_dict[seq_name]:
			#1.perfectly match
			if gene in anno_dict[seq_name]:
				perfect_anno_dict[seq_name].append(gene)
			elif gene[0] in [i[0] for i in anno_dict[seq_name]]:
				start_anno_dict[seq_name].append(gene)
			elif gene[1] in [i[1] for i in anno_dict[seq_name]]:
				end_anno_dict[seq_name].append(gene)
			else:
				neither_anno_dict[seq_name].append(gene)

	result_file = open('my_fraction_anno.gff3', 'w')
	result_file.write('###  Perfectly match both ends of one of my predicted genes\n')
	for seq_name in anno_dict:
		for gene in perfect_anno_dict[seq_name]:
			result_file.write(seq_name+'\t ena\t CDS \t {}\t {}\t . \t +\t 0\t .\n'.format(gene[0], gene[1]))
	result_file.write('###  Match the start but not the end of a predicted gene\n')
	for seq_name in anno_dict:
		for gene in start_anno_dict[seq_name]:
			result_file.write(seq_name+'\t ena\t CDS \t {}\t {}\t . \t +\t 0\t .\n'.format(gene[0], gene[1]))
	result_file.write('###  Match the end but not the start of a predicted gene\n')
	for seq_name in anno_dict:
		for gene in end_anno_dict[seq_name]:
			result_file.write(seq_name+'\t ena\t CDS \t {}\t {}\t . \t +\t 0\t .\n'.format(gene[0], gene[1]))
	result_file.write('###  Do not match neither the start not the end of a predicted gene\n')
	for seq_name in anno_dict:
		for gene in neither_anno_dict[seq_name]:
			result_file.write(seq_name+'\t ena\t CDS \t {}\t {}\t . \t +\t 0\t .\n'.format(gene[0], gene[1]))
	result_file.close()


anno_dict, seq_name_list = read_anno()
my_anno_dict = read_my_anno()
report_anno()
report_my_anno()