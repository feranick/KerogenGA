#!/bin/bash
rm -r bin
mkdir bin
javac -d bin -cp lib/\* src/general/*.java src/GeneticAlgorithm/*.java
#cp -r lib/*.jar  bin
cp -r lib/MANIFEST.MF bin
cd bin
jar cvfm ../kga.jar MANIFEST.MF *

