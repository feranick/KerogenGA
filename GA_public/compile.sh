#!/bin/bash
javac -d bin -cp lib/\* src/general/*.java src/GeneticAlgorithm/*.java
cp -r lib/* bin
cd bin
jar cvfe ../kga.jar general.MainDriver *

