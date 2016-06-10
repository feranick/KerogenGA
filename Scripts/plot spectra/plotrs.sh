#!/bin/bash
#
#  plotrs.sh
#
#  Plot and save ASCII files as png
#  
#  Gnuplot parameters are set in param.plg

parfile="/usr/local/bin/param.plg"
parfile2="/usr/local/bin/param-2.plg"

if [[ -z $2 ]]
   then
	output=$(echo "$1" | sed 's/.\{4\}$//')".png"
	gnuplot -e "datafile='$1'; outputname='$output'" $parfile
	echo " Saved plot in:" $output  
   else
	output=$(echo "$1" | sed 's/.\{4\}$//')"_with-fit.png"
	gnuplot -e "datafile='$1'; datafile2='$2'; outputname='$output'" $parfile2
   	echo " Saved plot with best fit in:" $output
   fi
