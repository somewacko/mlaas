f = open('../data/states_window.txt','w')
states= open('../data/windowedStates.csv','r')
for each in states.readlines()[:76000]:
        state=map(int,each.strip().split(',')[1:])
        f.write(str(state.index(max(state))))
        f.write('\n')
f.close()
states.close()
