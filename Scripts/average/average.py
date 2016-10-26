#!/usr/bin/env python
# -*- coding: utf-8 -*-
'''
**********************************************************
*
* Average
* Take average on all ASCII files in folder.
* version: 20161026a
*
* By: Nicola Ferralis <feranick@hotmail.com>
*
***********************************************************
'''
print(__doc__)

import numpy as np
import sys, os.path, getopt, glob, csv
from os.path import exists
from os import rename
from datetime import datetime, date

cvs = False

#**********************************************
''' Main '''
#**********************************************
def main():
    outputName = os.path.relpath(".","..") + '_average' + str(datetime.now().strftime('_%Y-%m-%d_%H-%M-%S.txt'))
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
    saveData(outputName, Xa, Ya, False)
    print('\n Average saved in: ' + outputName)


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

#**********************************************
''' Save average file '''
#**********************************************

def saveData(inputFile, x, y, cvs):
    
    with open(inputFile, "a") as xyFile:
        for i in range(0, len(x)):
            
            if cvs==True:
                xyFile.write('{:},'.format(x[i]))
                xyFile.write('{:},'.format(y[i]))
            else:
                xyFile.write('{:}\t'.format(x[i]))
                xyFile.write('{:}\n'.format(y[i]))
        xyFile.close()

#************************************
''' Main initialization routine '''
#************************************
if __name__ == "__main__":
    sys.exit(main())
