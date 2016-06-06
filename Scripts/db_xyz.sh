#!/bin/bash
#
# Extract XYZ coordinate file from MongoDB Molecule
#
# version 1.5-20160605a
#
# Nicola Ferralis <ferralis@mit.edu>
#
#
# License: GNU General Public License v.2 or above
#
# Usage: db_xyz.sh <name molecule>


if [[ $1 == "-v" ]]
     then
	echo
	echo " Version 1.5-20160605a"
        echo
	exit
     fi

if [[ -z $1 ]]
     then
	echo
	echo " Usage: db_xyz.sh <name molecule>"
	echo
	exit
     fi

molecule="$1"
database="shale"

num ()
{
    if [[ $1 == *[,]* ]]
    then
        echo "$1" | sed 's/.$//'
    else
        echo "$1"
    fi
}


molinfo=$(echo "db.molecule.find({name:\"$1\"})" | mongo $database)
molstructname=$(echo $molinfo | sed 's/\" }.*$//' | sed 's/.*\"structurename\" : \"//')
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

    #echo "I = " $i  "WORD: " $word
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


echo "XYZ coordinates saved in: " $outfile

