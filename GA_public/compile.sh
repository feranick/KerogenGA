#!/bin/bash
javac -cp lib/\* src/general/*.java src/GeneticAlgorithm/*.java
cp -r lib/* bin
cd bin
jar cf ../ga.jar *

