Genetic Algorithm to identify molecular compositions from the Raman spectra of
heterogeneous organic matter.

Direct use of this code requires a Raman spectra database running in the background.

The Genetic Algorithm part is a standalone module. It can be used as a generic tool to find 
a linear combination of vectors that matches closely to another vector.

Requirements
=============

This code requires mongodb java driver library and apache math library. Tested with:

- mongo-java-driver (version: 2.11.1 or higher; not compatible with v. 3.x), available here:
https://oss.sonatype.org/content/repositories/releases/org/mongodb/mongo-java-driver

- apache commons-math3 (version: 3.3 or higher). 
http://commons.apache.org/proper/commons-math/download_math.cgi

Scripts
========

Also provided are bash scripts to extract molecular structures from the identified molecular fingerprints.


Compilation and installation
=============================

This is compatible only with UNIX systems (Linux, MacOS). Run the following script:

./compile.sh

Copy the binary kga.jar in the location where it can be found using bash. Copy the folder "lib" in the same location as kga.jar.

