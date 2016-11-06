#!/bin/bash
#
# Extract concentrations  from Kerogen-genome search
#
# version 1.0-20161106a
#
# Nicola Ferralis <ferralis@mit.edu>
#
# License: GNU General Public License v.2 or above
#

filename="bestIndividual.txt"

if [[ $1 == "-v" ]]
     then
	echo
	echo " Version 1.0-20161106a"
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



outfilePC="bestIndividual_concentrations.csv"

num ()
{
    if [[ $1 == *[,]* ]]
    then
        echo "$1" | sed 's/.$//'
    else
        echo "$1"
    fi
}

totconc=0
while read -r line
do
    coeff=$(echo $line | sed 's/.*: //')
    coeff=`echo ${coeff} | sed -e 's/[eE]+*/\\*10\\^/'`
    totconc=$(echo "$totconc+$coeff" | bc)
    #echo = "coeff: "$coeff
    #echo "$coeff" | bc
done <  <(tr -d '\r' < "$filename")

echo

while read -r line
do
    name=$(echo $line | sed 's/ :.*$//')
    coeff=$(echo $line | sed 's/.*: //')
    coeff=`echo ${coeff} | sed -e 's/[eE]+*/\\*10\\^/'`
    conc=$(echo "scale=2; 100*$coeff/$totconc" | bc)
    
    echo "Molecule: "$name
    echo " Linear combination coefficient: "$coeff
    echo " Concentration (%): "$conc
    echo " Total Concentration: "$totconc

    echo $name","$conc >> $outfilePC
    echo 

done < "$filename"

    echo " Concentrations saved in: " $outfilePC
    echo
