#!/usr/bin/env python
import numpy as np
import scipy.io
sample_size=76000
data =np.loadtxt("../data/new_files/window_features4000.txt", delimiter=',', usecols=range(0,969))
#skip last column which is label
print data.shape
print len(data[0]), len(data)
#make the data matrix DxN
data=np.transpose(data)
scipy.io.savemat('/Users/bimangujral/Documents/MATLAB/baft'+str(sample_size)+'.mat', mdict={'arr': data})
