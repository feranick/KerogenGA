#!/usr/bin/env python
# -*- coding: utf-8 -*-
'''
**********************************************************
*
* CollectGA
*
* version: 20161102a
*
* By: Nicola Ferralis <feranick@hotmail.com>
*
* help: python collectGA.py -h
*
***********************************************************
'''
print(__doc__)

import numpy as np
import sys, os.path, getopt, glob, csv
from datetime import datetime, date
from pandas import read_csv

#inputFile = 'bestIndividual_concentrations.csv'
summaryFile = "summaryFile.csv"
summaryFolder = os.getenv("HOME") + '/Desktop/'


#**********************************************
''' Main '''
#**********************************************
def main():
        inputFile = sys.argv[1]
        runCollect(inputFile)

#**********************************************
''' RunCollection '''
#**********************************************

def runCollect(inputFile):
    print ('New GA file: ' + os.path.relpath(".","..") + '\n')
    summaryFile_path = summaryFolder + summaryFile
    outputName = os.path.relpath(".","..") + '_collect' + '.csv'

    #**********************************************
    ''' Read new file '''
    #**********************************************
    X = readFile(inputFile)
    
    #**********************************************
    ''' Read summary file '''
    #**********************************************
    ind = 0
    if os.path.exists(summaryFile_path) == True:
        L,llr = readSummary(summaryFile_path)
        lr = [None]*llr
    else:
        with open(summaryFile_path, 'a') as f:
            csv_out=csv.writer(f)
            csv_out.writerow(np.append(['name'], X[:,0]))
            f.close()
        L = np.append(['name'], X[:,0]).tolist()
        lr = np.append(os.path.relpath(".",".."), X[:,1]).tolist()

    summary = lr
    summary[0] = os.path.relpath(".","..")
    origL = len(L)

    #**********************************************
    ''' Detect if molecule has been already seen '''
    #**********************************************
    for i in range(0,len(X)):
        if X[i,0] in L:
            j = L.index(X[i,0])
            print(L[j] + '\t already detected')
            summary[j] = X[i,1]
        else:
            L.append(X[i,0])
            summary[i+1] = ''
            print('\033[1m' + L[j] + '\t first detected!' + '\033[0m')
            summary = np.append(summary, X[i,1])
            ind += 1

    #*********************************************************
    ''' Change header file if new molcules are detected '''
    #*********************************************************
    if ind != 0:
        df = read_csv(summaryFile_path)
        for i in range(0,ind):
            df.insert(i+origL, L[i+origL], '', 0)
        df.columns = L
        df.to_csv(summaryFile_path, index=False)

    #**********************************************
    ''' Save new data into Summary '''
    #**********************************************

    with open(summaryFile_path, 'a') as f:
        csv_out=csv.writer(f)
        csv_out.writerow(summary)
        f.close()

    print('\n Summary saved in: ' + summaryFile_path + '\n')


#**********************************************
''' Read new files '''
#**********************************************

def readFile(sampleFile):
    try:
        X=np.empty([0,2])
        with open(sampleFile, 'r') as f:
            data = csv.reader(f, delimiter=',')
            for row in data:
                X = np.row_stack((X, [np.array(row[0]), np.array(row[1], dtype=float)]))

    except:
        print('\033[1m' + '\n Sample data file not found \n ' + '\033[0m')
        return

    return X


#**********************************************
''' Read Summary '''
#**********************************************

def readSummary(summaryFile):
    try:
        with open(summaryFile, 'r') as f:
            data = csv.reader(f, delimiter=',')
            L = next(data)
        with open(summaryFile, 'r') as f:
            lastrow = None
            for lastrow in csv.reader(f): pass
    except:
        print('\033[1m' + '\n Sample data file not found \n ' + '\033[0m')
        return
    
    return L, len(lastrow)

#************************************
''' Lists the program usage '''
#************************************
def usage():
    print('\n Usage:')
    print('  python collectGA.py \n')



#************************************
''' Main initialization routine '''
#************************************
if __name__ == "__main__":
    sys.exit(main())
