Plotrs.sh takes ASCII file with Raman spectral activity and plots into png. This scripts uses gnuplot for plotting, so it must be installed in the user system. 

Installation
=============

Copy files (plotrs.sh, param.plg, param-2.plg) in /usr/local/bin or where user accessible binaries are usually installed. The script and plg files can be copied around the folder where the ASCII files are. In any case, the location of the plg files needs to be adjusted in plotrs.sh for the script to work.

Usage
======

This assumes the scripts are located in the user accessible binary folder.
For single individual spectra run:

	plotrs.sh <raman spectra file>


For plotting two individual spectra on the same plot:

	plotrs.sh <raman spectra file 1> <raman spectra file 2> 

For batch, run:

	find . -name '*_RS.txt' -exec plotrs.sh {} \;

Change '*.RS.txt' to fit the extension of the ASCII files.

