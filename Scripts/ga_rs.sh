#!/bin/bash
#
# Extract Raman Spectra from Kerogen-genome search
#
# version 1.0-20160607b
#
# Nicola Ferralis <ferralis@mit.edu>
#
# License: GNU General Public License v.2 or above
#

filename="bestIndividual.txt"

if [[ $1 == "-v" ]]
     then
	echo
	echo " Version 1.0-20160607b"
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
    totconc=$(echo "$totconc+$coeff" | bc)

done <  <(tr -d '\r' < "$filename")

echo

while read -r line
do
    name=$(echo $line | sed 's/ :.*$//')
    coeff=$(echo $line | sed 's/.*: //')
    conc=$(echo "scale=2; 100*$coeff/$totconc" | bc)
    
    echo "Molecule: "$name
    echo " Linear combination coefficient: "$coeff
    echo " Concentration (%): "$conc

    molinfo=$(echo "db.molecule.find({name:\"$name\"})" | mongo $database)

    numrings=$(echo $molinfo | sed 's/, \"raman.*$//' | sed 's/.*\"numberofrings\" ://')
    echo " Number of aromatic rings: "$numrings

    molstructname=$(echo $molinfo | sed 's/\" }.*$//' | sed 's/.*\"structurename\" : \"//')
    echo " Molecular structurename: "$molstructname

    outfile=$molstructname"_RS.txt"

    molstructure=$(echo "db.structure.find({structurename:\"$molstructname\"})" | mongo $database)

    numberatoms=$(echo $molstructure | grep -o "index" | wc -l)

    ramanspectrumId=$(echo $molinfo | sed 's/\"), \"str.*$//' | sed 's/.*\"ramanspectrum_id" : ObjectId(\"//')
    echo " Raman spectrum ID: "$ramanspectrumId

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

    echo " Raman activityes saved in: "$outfile
    echo $name","$conc >> $outfilePC
    echo 

done < "$filename"

    echo " Concentrations saved in: "$outfilePC
    echo
