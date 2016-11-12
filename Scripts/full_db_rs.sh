#!/bin/bash
#
# Create Raman spectra of each molecule in Kerogen-genome DB
#
# version 2.0-20161111a
#
# Nicola Ferralis <ferralis@mit.edu>
#
# License: GNU General Public License v.2 or above
#

filename="checkpoint.conversion.txt"

width=10.0
laser=630.0
infreq=0.0
enfreq=1800.0
stepfreq=1.0

if [[ $1 == "-v" ]]
     then
	echo
	echo " Version 2.0-20161111a"
        echo
	exit
     fi

if [ ! -f $filename ]
     then
	echo
	echo " Required file:" $filename "does not exist"
        echo
	exit
     fi

logfile="log-rs.txt"

while read -r line
do
    number=$(echo $line | sed 's/ :.*$//')
    name=$(echo $line | sed 's/.*: //')
    java -jar /export/apps/kga.jar --spectra $name --laser $laser --width $width --infreq $infreq --enfreq $enfreq --stepfreq $stepfreq

done < "$filename"
