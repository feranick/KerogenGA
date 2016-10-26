#!/bin/bash
#
# Submit qsub job and handle files for GA search
#
# version 1.0-20161026a
#
# Nicola Ferralis <ferralis@mit.edu>
#
#
# License: GNU General Public License v.2 or above
#
# Usage: rga.sh <txt file with txt extension>

namedir=$(echo $1 | sed 's/.txt.*$//')
mkdir $namedir
cp $1 $namedir
cd $namedir
sub_ga 12 "$1" | qsub -N "$namedir"

#java -jar /export/apps/gt.jar --laser 633.0 --NIST --diamondoid --elitefit "$2" "$1" > output.txt

