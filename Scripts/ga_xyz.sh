#!/bin/bash
#
# Extract XYZ coordinate file from Kerogen-genome search
#
# version 1.0-20150626a
#
# Nicola Ferralis <ferralis@mit.edu>
#
# License: GNU General Public License v.2 or above
#

filename="bestIndividual.txt"
database="shale"

while read -r line
do
    name=$(echo $line | sed 's/ :.*$//')
    fitness=$(echo $line | sed 's/.*: //')

    echo "Molecule: "  $name
    echo " Fitness: " $fitness

    molinfo=$(echo "db.molecule.find({name:\"$name\"})" | mongo $database)
    #echo $molinfo

    molstructname=$(echo $molinfo | sed 's/ }.*$//' | sed 's/.*\"structurename\" ://')
    echo " Molecular structurename: " $molstructname

    outfile=$molstructname".xyz"

    numrings=$(echo $molinfo | sed 's/, \"raman.*$//' | sed 's/.*\"numberofrings\" ://')
    echo " Number of aromatic rings: " $numrings

    molstructure=$(echo "db.structure.find({structurename:$molstructname})" | mongo $database)
    echo $molstructure

    numberatoms=$(echo $molstructure | grep -o "index" | wc -l)
    echo $numberatoms >> $outfile
    echo $molstructname  >> $outfile

    molstructure=$(echo $molstructure | sed 's/.*\"atoms\" : \[ //' )
    i=0

    for word in $molstructure;
    do
        if [ $word = "\"index\"" ] then
            ((i=0))
        fi

        if [ $i -eq 2 ] then
            index=$(echo $word | sed 's/.$//')
        fi

        if [ $i -eq 5 ] then
            element=$(echo $word | sed 's/.$//')
        fi

        if [ $i -eq 8 ] then
            x=$(echo $word | sed 's/.$//')
        fi

        if [ $i -eq 11 ] then
            y=$(echo $word | sed 's/.$//')
        fi

        if [ $i -eq 14 ]  then
            z=$(echo $word | sed 's/.$//')

            if [ $element = "1" ]  then
                echo "C $x $y $z" >> $outfile
            else
                echo "H $x $y $z" >> $outfile
            fi
        fi
        ((i++))

    done

    echo "XYZ coordinates saved in: " $outfile
    echo

done < "$filename"