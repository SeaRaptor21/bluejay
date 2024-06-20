import os
import timeit

CMDS = [
    'python tests/bubbleSort.py',
    'java com.esjr.bluejay.Main tests/bubbleSort.blu'
]
times = []
for cmd in CMDS:
    times.append(timeit.timeit(lambda: os.system(cmd), number=1))
#for cmd, time in zip(CMDS, times):
    #print(f'{cmd} took {time} seconds.')
#print(f'Command #2 is {times[1]/times[0]} times slower than Command #1')
print(times[1]/times[0])