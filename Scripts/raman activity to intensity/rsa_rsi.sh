#!/bin/bash
#
# Convert Raman spectral activities to Raman Intensities
#
# version 1.0-20160608a
#
# Nicola Ferralis <ferralis@mit.edu>
#
#
# License: GNU General Public License v.2 or above
#
# Usage: rsa_rsi.sh <Raman activity file> <excitation in nm>
#
# The conversion is done according to:
#  Chaitanya et al, J. At. Mol. Sci. 3 (2012), 2012 
#  Porezag et al. PRB 54 (1996), 7830
#


if [[ $1 == "-v" ]]
     then
	echo
	echo " Version 1.0-20160608a"
        echo
	exit
     fi

if [[ -z $1 ]]
     then
	echo
	echo " Usage: rsa_rsi.sh <Raman activity file> <excitation in nm>"
	echo
	exit
     fi

echo

if [[ -z $2 ]]
     then
	laserw=633
	echo " Excitation wavelenght set automatically as: "$laserw "nm"
     else
	laserw=$2
	echo " Excitation wavelenght: "$laserw "nm"
     fi

echo

excit=$(echo "9992397.506/$laserw" | bc -l )
factor=1*10^-16

outfile=$(echo "$1" | sed 's/.\{4\}$//')"I-"$laserw"nm.txt"

i=0
while read -r line
do
    for word in $line;
    do
    	if [ $i -eq 0 ]
    	then
    		frequency=$word
    		((i=1))

    	else  
    		activity=$word
    		boltz=$(echo "1-e(- k_h*k_c*$frequency/(k_b*300))" | bc -l)
        	intensity=$(echo "round($factor*($excit-$frequency)^4*$activity/($frequency*$boltz),4)" | bc -l)
		((i=0))
    	fi
    done
    echo $frequency $intensity >> $outfile
done < "$1"

echo "Raman Intensities saved in saved in: "$outfile

