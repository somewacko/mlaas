#!/usr/bin/env python
import os
path = '../data/features/'  # remove the trailing '\'
windowed_path = '../data/windowedStates.csv' 
window_features=[]
for dir_entry in os.listdir(path):
    dir_entry_path = os.path.join(path, dir_entry)
    if os.path.isfile(dir_entry_path):
	with open(dir_entry_path,'r') as f:
		pick_index = 25
		line = f.readline()
		fv=line.strip().split('{')
		#because 1001 parts, first is just '['
		del fv[0]
		while(pick_index<950):
			fv[pick_index]='{'+fv[pick_index]
			fv_dict = eval(fv[pick_index].strip(', '))
			del fv_dict['hbonds']
			window_features.append(fv_dict)
			pick_index+=50
		print dir_entry_path 
#dump to file in csv format
#chi1,chi2,hbonds,rms,state
#1 FV per line
f = open('../data/window_features'+str(len(window_features))+'.txt','w')
states= open('../data/windowedStates.csv','r')
for each in window_features:
	fv=''
	#can choose key inputs here 
	#if only subset of features are required
	for v in each.values():
		for val in v:
			fv=fv+str(val)+','
	state=map(int,states.readline().strip().split(',')[1:])
	fv=fv+state.index(max(state))
	fv=fv+'\n'
	f.write(fv)
f.close()
states.close()
print len(window_features)
