#!/bin/bash
#
#  plotrs.sh
#
#  Plot and save ASCII files as png
#  
#  Gnuplot parameters are set in param.plg

parfile="/usr/local/bin/param.plg"
parfile2="/usr/local/bin/param-2.plg"
parfile3="/usr/local/bin/param-3.plg"

if [[  $3 ]]
   then
	echo " 3 params"
	output=$(echo "$1" | sed 's/.\{4\}$//')"_with-fit-1.png"
        gnuplot -e "datafile='$1'; datafile2='$2'; datafile3='$3'; outputname='$output'" $parfile3
        echo " Saved plot in:" $output
   else
   if [[ $2 ]]
   	then
		echo "2 params"
		output=$(echo "$1" | sed 's/.\{4\}$//')"_with-fit.png"
		gnuplot -e "datafile='$1'; datafile2='$2'; outputname='$output'" $parfile2
		echo " Saved plot in:" $output  
   	else
		echo " 1 param"
		output=$(echo "$1" | sed 's/.\{4\}$//')".png"
		gnuplot -e "datafile='$1'; outputname='$output'" $parfile
   		echo " Saved plot with best fit in:" $output
   	fi
   fi
