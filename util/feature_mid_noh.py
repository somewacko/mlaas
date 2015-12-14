#!/usr/bin/env python
import os
path = '/mddb2/backup/projects/bpti_db/features/'  # remove the trailing '\'
mid_features={}
for dir_entry in os.listdir(path):
    dir_entry_path = os.path.join(path, dir_entry)
    if os.path.isfile(dir_entry_path):
	with open(dir_entry_path,'r') as f:
		file_number = dir_entry.split('_')[1].split('.')[0]
		file_number = int(file_number)
		line = f.readline()		#data is not delimited by line
		fv=line.strip().split('{')
		#because 1001 parts, first is just '['
		l =len(fv)
		#print l	#1001
		#print fv[0]	#'['
		fv[501]='{'+fv[501]
		fv_mid_dict= (eval(fv[501].strip(', ')))
		del fv_mid_dict['hbonds']
		#print len(fv_mid_dict.keys())
		print dir_entry_path 
		mid_features[file_number]=fv_mid_dict
		f.close()
#dump to file in csv format
#chi1,chi2,hbonds,rms,state
#1 FV per line
f = open('../new_files/mid_noh_features'+str(len(mid_features))+'.txt','w')
state_file= open('../data/states.txt','r')
for i, state in enumerate(state_file.readlines()[:4000]):
        #for each in avg_features:
        fv=''
        #can choose key inputs here 
        #if only subset of features are required
        for v in mid_features[i].values():
                for val in v:
                        fv=fv+str(val)+','
        fv=fv+state.strip().split()[1]
        fv=fv+'\n'
        f.write(fv)
f.close()
state_file.close()
print len(mid_features)
