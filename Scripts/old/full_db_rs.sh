#!/bin/bash
#
# Create individual XYZ coordin from full Kerogen-genome DB
#
# version 1.0-20160607a
#
# Nicola Ferralis <ferralis@mit.edu>
#
# License: GNU General Public License v.2 or above
#

filename="checkpoint.conversion.txt"

if [[ $1 == "-v" ]]
     then
	echo
	echo " Version 1.0-20160607a"
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
logfile="log-rs.txt"

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
    echo " Molecular structurename: "$molstructname >> $logfile

    outfile=$number"_"$molstructname"_RS.txt"

    numrings=$(echo $molinfo | sed 's/, \"raman.*$//' | sed 's/.*\"numberofrings\" ://')
    echo " Number of aromatic rings: "$numrings >> $logfile

    molstructure=$(echo "db.structure.find({structurename:\"$molstructname\"})" | mongo $database)
    #echo $molstructure

    numberatoms=$(echo $molstructure | grep -o "index" | wc -l)
    echo " Number of atoms: "$numberatoms >> $logfile
    
    ramanspectrumId=$(echo $molinfo | sed 's/\"), \"str.*$//' | sed 's/.*\"ramanspectrum_id" : ObjectId(\"//')
    
    echo " Raman spectrum ID: "$ramanspectrumId >> $logfile

    ramanspectrumInfo=$(echo "db.ramanspectra.find({\"_id\":ObjectId(\"$ramanspectrumId\")})" | mongo $database)
    rs=$(echo $ramanspectrumInfo | sed 's/.*\"content\" : \[ //' )
    i=0

    for word in $rs;
        do
        if [ $word = "\"frequency\"" ]
        then
            ((i=0))
        fi

        if [ $i -eq 2 ]
        then
            frequency=$(num $word)
        fi

        if [ $i -eq 5 ]
        then
            activity=$(num $word)
            echo "$frequency $activity" >> $outfile
	fi

        ((i++))

    done

    echo " Raman activity saved in: "$outfile >> $logfile
    echo >> $logfile

done < "$filename"

echo "Saved " $number "raman spectra." >> $logfile
