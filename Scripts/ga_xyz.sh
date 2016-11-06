#!/bin/bash
#
# Extract XYZ coordinate file from Kerogen-genome search
#
# version 1.6.-20161106a
#
# Nicola Ferralis <ferralis@mit.edu>
#
# License: GNU General Public License v.2 or above
#

filename="bestIndividual.txt"

if [[ $1 == "-v" ]]
     then
	echo
	echo " Version 1.6-20161106a"
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


database="shale"
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

    molinfo=$(echo "db.molecule.find({name:\"$name\"})" | mongo $database)

    numrings=$(echo $molinfo | sed 's/, \"raman.*$//' | sed 's/.*\"numberofrings\" ://')
    echo " Number of aromatic rings: "$numrings

    molstructname=$(echo $molinfo | sed 's/\" }.*$//' | sed 's/.*\"structurename\" : \"//')
    echo " Molecular structurename: "$molstructname

    outfile=$molstructname".xyz"

    molstructure=$(echo "db.structure.find({structurename:\"$molstructname\"})" | mongo $database)

    numberatoms=$(echo $molstructure | grep -o "index" | wc -l)
    echo $numberatoms >> $outfile
    echo $molstructname  >> $outfile

    molstructure=$(echo $molstructure | sed 's/.*\"atoms\" : \[ //' )
    i=0

    for word in $molstructure;
    do
        if [ $word = "\"index\"" ]
        then
            ((i=0))
        fi

        if [ $i -eq 2 ]
        then
            index=$(num $word)
        fi

        if [ $i -eq 5 ]
        then
            element=$(num $word)
        fi

        if [ $i -eq 8 ]
        then
            x=$(num $word)
        fi

        if [ $i -eq 11 ]
        then
            y=$(num $word)
        fi

        if [ $i -eq 14 ]
        then
            z=$(num $word)

            if [ $element = "1" ]
            then
                echo "C $x $y $z" >> $outfile
            else
                echo "H $x $y $z" >> $outfile
            fi
        fi
        ((i++))

    done

    echo " XYZ coordinates saved in: " $outfile
    echo $name","$conc >> $outfilePC
    echo 

done < "$filename"

    echo " Concentrations saved in: " $outfilePC
    echo
