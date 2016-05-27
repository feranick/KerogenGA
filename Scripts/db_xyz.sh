#!/bin/bash
#
# Extract XYZ coordinate file from MongoDB Molecule
#
# version 1.0-20150626a
#
# Nicola Ferralis <ferralis@mit.edu>
#
#
# License: GNU General Public License v.2 or above
#
# Usage: db_xyz.sh <name molecule>

molecule="$1"
outfile=$molecule".xyz"

database="shale"

molinfo=$(echo "db.molecule.find({name:\"$1\"})" | mongo $database)
molstructname=$(echo $molinfo | sed 's/ }.*$//' | sed 's/.*\"structurename\" ://')
molstructure=$(echo "db.structure.find({structurename:$molstructname})" | mongo $database)

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

