#!/usr/bin/env python
# -*- coding: utf-8 -*-
'''
**********************************************************
*
* Average
* Take average on all ASCII files in folder.
* version: 20161027a
*
* By: Nicola Ferralis <feranick@hotmail.com>
*
* help: python average.py -h
*
***********************************************************
'''
print(__doc__)

import numpy as np
import sys, os.path, getopt, glob
from datetime import datetime, date

#**********************************************
''' Main '''
#**********************************************
def main():

    try:
        opts, args = getopt.getopt(sys.argv[1:], "c", ["csv"])
    except:
        usage()
        sys.exit(2)
    
    if opts == []:
        runAverage(False)
    
    for o, a in opts:
        if o in ("-c" , "--csv"):
            runAverage(True)
            sys.exit(2)

#**********************************************
''' RunAverage '''
#**********************************************

def runAverage(csv):
    outputName = os.path.relpath(".","..") + '_average' + str(datetime.now().strftime('_%Y-%m-%d_%H-%M-%S'))
    i = 0
    for f in glob.glob('*.txt'):
        i+=1
        X, Y = readFile(f)
        try:
            Xa
            Ya
        except:
            Xa = X
            Ya = Y
        else:
            Ya += Y
    Ya /= i

    if csv==True:
        delim = ','
        outputName += '.csv'
    else:
        delim = '\t'
        outputName += '.txt'

    np.savetxt(outputName, np.transpose([Xa,Ya]), delimiter=delim, fmt='%.5f')

    print('\n Average saved in: ' + outputName + '\n')


#**********************************************
''' Read and compute averages '''
#**********************************************

def readFile(sampleFile):
    try:
        with open(sampleFile, 'r') as f:
            print(' Opening file: ' + sampleFile)
            Rtot = np.loadtxt(f, unpack =True)
    except:
        print('\033[1m' + '\n Sample data file not found \n ' + '\033[0m')
        return
    
    Y=Rtot[1,:]
    X=Rtot[0,:]
    return X, Y


#************************************
''' Lists the program usage '''
#************************************
def usage():
    print('\n Usage:')
    print(' Output as ASCII txt file')
    print('  python average.py \n')
    print(' Output as csv txt file')
    print('  python average.py -c \n')
    print('  python average.py --csv\n')


#************************************
''' Main initialization routine '''
#************************************
if __name__ == "__main__":
    sys.exit(main())
