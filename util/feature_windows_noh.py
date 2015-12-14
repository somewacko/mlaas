#!/usr/bin/env python
import os
path = '/mddb2/backup/projects/bpti_db/features/'  # remove the trailing '\'
window_features={}
for dir_entry in os.listdir(path):
    dir_entry_path = os.path.join(path, dir_entry)
    if os.path.isfile(dir_entry_path):
	with open(dir_entry_path,'r') as f:
		file_number = dir_entry.split('_')[1].split('.')[0]
		pick_index = 25
		line = f.readline()
		fv=line.strip().split('{')
		#because 1001 parts, first is just '['
		file_number=int(file_number)
		window_features[file_number]=[]
		del fv[0]
		while(pick_index<950):
			fv[pick_index]='{'+fv[pick_index]
			fv_dict = eval(fv[pick_index].strip(', '))
			del fv_dict['hbonds']
			window_features[file_number].append(fv_dict)
			pick_index+=50
		print dir_entry_path 
#dump to file in csv format
#chi1,chi2,hbonds,rms,state
#1 FV per line
f = open('../new_files/window_noh_features'+str(len(window_features))+'.txt','w')
state_file= open('../data/windowedStates.csv','r')
for i, state in enumerate(state_file.readlines()[:76000]): 
	#19*4000
        fv=''
        (file_number,rem) = state.strip().split(':')
	file_number=int(file_number)
	rem=rem.split(',')
	list_index=int(rem[0])/50;
	state=map(int,rem[1:])
	#can choose key inputs here 
        #if only subset of features are required
        for v in window_features[file_number][list_index].values():
                for val in v:
                        fv=fv+str(val)+','
        fv=fv+str(state.index(max(state)))
        fv=fv+'\n'
        f.write(fv)
f.close()
state_file.close()
print len(window_features)
