#!/bin/bash
#
# Create individual XYZ coordinate file from full Kerogen-genome DB
#
# version 1.5-20160605a
#
# Nicola Ferralis <ferralis@mit.edu>
#
# License: GNU General Public License v.2 or above
#

filename="checkpoint.conversion.txt"

if [[ $1 == "-v" ]]
     then
	echo
	echo " Version 1.5-20160605a"
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
logfile="log.txt"

num ()
{
    if [[ $1 == *[,]* ]]
    then
        echo "$1" | sed 's/.$//'
    else
        echo "$1"
    fi
}

while read -r line
do
    number=$(echo $line | sed 's/ :.*$//')
    name=$(echo $line | sed 's/.*: //')

    echo "Molecule: "  $name >> $logfile
    echo " Index: " $number >> $logfile

    molinfo=$(echo "db.molecule.find({name:\"$name\"})" | mongo $database)
    #echo $molinfo

    molstructname=$(echo $molinfo | sed 's/\" }.*$//' | sed 's/.*\"structurename\" : \"//')
    echo " Molecular structurename: " $molstructname >> $logfile

    outfile=$number"_"$molstructname".xyz"

    numrings=$(echo $molinfo | sed 's/, \"raman.*$//' | sed 's/.*\"numberofrings\" ://')
    echo " Number of aromatic rings: " $numrings >> $logfile

    molstructure=$(echo "db.structure.find({structurename:\"$molstructname\"})" | mongo $database)
    #echo $molstructure

    numberatoms=$(echo $molstructure | grep -o "index" | wc -l)
    echo " Number of atoms: "$numberatoms >> $logfile
    
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

    echo " XYZ coordinates saved in: " $outfile >> $logfile
    echo >> $logfile

done < "$filename"

echo "Saved " $number "structures in XYZ files." >> $logfile
