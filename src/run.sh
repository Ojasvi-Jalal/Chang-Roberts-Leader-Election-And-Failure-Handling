#!/bin/bash

# First, compile the Java programs into Java classes
javac Network.java
javac Node.java

# Now pass the arguments to the Java classes
java Network $1 $2