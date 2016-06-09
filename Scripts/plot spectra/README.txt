Plotrs.sh takes ASCII file with Raman spectral activity and plots into png.

For individual spectra run:

	./plotrs.sh <raman spectra file>

For batch, place both files (plotrs.sh and param.plg) inside the folder with the file to be 
plotted. Then run:

	find . -name '*_RS.txt' -exec ./plotrs.sh {} \;

Change '*.RS.txt' to fit the extension of the ASCII files.
