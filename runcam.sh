#!/bin/sh

EXAMPLE_CP=lib/unirest-java-1.4.5.jar:lib/httpclient-4.1.3.jar:lib/httpmime-4.3.6.jar:lib/org.json-20120521.jar:lib/httpcore-4.0.1:.

mkdir -p build && javac -classpath $EXAMPLE_CP -d build Main.java & java -classpath $EXAMPLE_CP:build Main "$@"