Genetic Algorithm to identify molecular compositions from the Raman spectra of
heterogeneous organic matter.

Direct use of this code requires a Raman spectra database running in the background.

The Genetic Algorithm part is a standalone module. It can be used as a generic tool to find 
a linear combination of vectors that matches closely to another vector.

This code requires mongodb java driver library and apache math library (tested using mongodb 
driver version 2.11.1 and apache math library 3.3). 

You can download them from their websites.

Also provided are bash scripts to extract molecular structures from the identified molecular fingerprints.