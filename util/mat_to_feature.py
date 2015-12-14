#!/usr/bin/env python
import numpy as np
import scipy.io
sample_size=76000
data_dict=scipy.io.loadmat('/Users/bimangujral/Documents/MATLAB/PCA40baft'+str(sample_size)+'.mat',variable_names='data')
print data_dict['data'].shape
data=data_dict['data']
#data=np.transpose(data_dict['data'])
#add states at the end of each feature
states=open('../data/states_window.txt')
sample_size=len(data[0])
states_row=[]
for i in range(0,sample_size):
	state=states.readline().strip()
	states_row.append(int(state))
print "States: " + str(len(states_row))
print "data: " + str(len(data))
#print data[:,0]
data=np.vstack([data,states_row])
#print data[:,0]
data=np.transpose(data)
#print data[0]
print len(data[0])
np.savetxt('../data/new_files/pca_window40_features4000.txt',data,delimiter=',',newline='\n')
states.close()
