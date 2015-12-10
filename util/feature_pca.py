#!/usr/bin/env python
import numpy as np

def center(data):
    return data - np.mean(data, axis=0)
def PCA(data, num_pc=2):
    #center data
    data = center(data)
    eigval,eigvec=np.linalg.eig(np.dot(data, np.transpose(data)))
    idx=np.argsort(eigval)[-num_pc:][::-1]
    eigval=eigval[idx]
    eigvec=eigvec[:,idx]
    return eigvec
#Read avg_feature file into an nXd matrix (n=4000,d=970)
data =np.loadtxt("../data/avg_features300.txt", delimiter=',', usecols=range(0,969))
#skip last column which is label
print data.shape
print len(data[0]), len(data)
#make the data matrix DxN
data=np.transpose(data)
eigvec=PCA(data)
print eigvec
