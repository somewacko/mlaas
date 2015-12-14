#!/usr/bin/env python
import os
path = '/mddb2/backup/projects/bpti_db/features/'  # remove the trailing '\'
avg_features={}
for dir_entry in os.listdir(path):
    dir_entry_path = os.path.join(path, dir_entry)
    if os.path.isfile(dir_entry_path):
	with open(dir_entry_path,'r') as f:
		file_number = dir_entry.split('_')[1].split('.')[0]
		print file_number
		file_number=int(file_number)
		line = f.readline()
		fv=line.strip().split('{')
		#because 1001 parts, first is just '['
		fv_dict=[]
		#print len(fv)
		l =len(fv)
		for i in range(1,l-1):
			fv[i]='{'+fv[i]
			fv_dict.append(eval(fv[i].strip(', ')))
		#print fv_dict[i-1]
		#for last record
		fv_dict.append(eval(('{'+fv[l-1]).strip(']').strip()))
		#print fv_dict[-1]
		#print len(fv_dict)
		avg_feat={}
		for (k,v) in fv_dict[0].items():
			avg_feat[k]=[0]*len(fv_dict[0][k])
		#print avg_feat.keys()
		for each in fv_dict:
			for k in each.keys():
				avg_feat[k]=[sum(i) for i in zip(avg_feat[k],each[k])]
		for k in avg_feat.keys():
			avg_feat[k]=[x/(float(len(fv_dict))) for x in avg_feat[k]]
		#print avg_feat
		#print dir_entry_path 
		del avg_feat['hbonds']
		avg_features[file_number]=avg_feat
		f.close()
#dump to file in csv format
#chi1,chi2,hbonds,rms,state
#1 FV per line
f = open('../new_files/avg_noh_features'+str(len(avg_features))+'.txt','w')
state_file= open('../data/states.txt','r')
for i, state in enumerate(state_file.readlines()[:4000]):
	#for each in avg_features:
	fv=''
	#can choose key inputs here 
	#if only subset of features are required
	for v in avg_features[i].values():
		for val in v:
			fv=fv+str(val)+','
	fv=fv+state.strip().split()[1]
	fv=fv+'\n'
	f.write(fv)
f.close()
state_file.close()
print len(avg_features)
