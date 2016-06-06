#!/bin/bash
#
# Extract XYZ coordinate file from MongoDB StructureName output
#
# version 1.1-20150626a
#
# Nicola Ferralis <ferralis@mit.edu>
#
#
# License: GNU General Public License v.2 or above
#

filename="$1"
outfile=$filename".xyz"

num ()
{
    if [[ $1 == *[,]* ]]
    then
        echo "$1" | sed 's/.$//'
    else
        echo "$1"
    fi
}

read -r line < $filename
numberatoms=$(echo $line | grep -o "index" | wc -l)
molname=$(echo $line | sed 's/\", \"molecule_id.*$//' | sed 's/.*\"moleculeName\" : \"//' )

echo $numberatoms >> $outfile
echo $molname  >> $outfile

line=$(echo $line | sed 's/.*\"atoms\" : \[ //' )
i=0

for word in $line;

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
        echo "Z: " $z

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