#!/bin/bash

data=$1
output=$1".png"
echo $output

gnuplot -e "datafile='$1'; outputname='$output'" param.plg


