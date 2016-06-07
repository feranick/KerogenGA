#!/bin/bash
#
# Extract Raman Activity from MongoDB Molecule
#
# version 1.0-20160607a
#
# Nicola Ferralis <ferralis@mit.edu>
#
#
# License: GNU General Public License v.2 or above
#
# Usage: db_rs.sh <name molecule>


if [[ $1 == "-v" ]]
     then
	echo
	echo " Version 1.0-20160607a"
        echo
	exit
     fi

if [[ -z $1 ]]
     then
	echo
	echo " Usage: db_rs.sh <name molecule>"
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
ramanspectrumId=$(echo $molinfo | sed 's/\"), \"str.*$//' | sed 's/.*\"ramanspectrum_id" : ObjectId(\"//')

#echo $ramanspectrumId
#echo
outfile=$molstructname"-RS.txt"

ramanspectrumInfo=$(echo "db.ramanspectra.find({\"_id\":ObjectId(\"$ramanspectrumId\")})" | mongo $database)

#echo $ramanspectrumInfo


rs=$(echo $ramanspectrumInfo | sed 's/.*\"content\" : \[ //' )
i=0

for word in $rs;
do
    if [ $word = "\"frequency\"" ]
    then
        ((i=0))
    fi

    #echo "I = " $i  "WORD: " $word
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


echo "Raman Activity saved in saved in: " $outfile

