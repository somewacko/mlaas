#!/usr/bin/env python
import os
path = '../features/'  # remove the trailing '\'
avg_features=[]
for dir_entry in os.listdir(path):
    dir_entry_path = os.path.join(path, dir_entry)
    if os.path.isfile(dir_entry_path):
	with open(dir_entry_path,'r') as f:
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
		print dir_entry_path 
		avg_features.append(avg_feat)
		f.close()
print len(avg_features)
