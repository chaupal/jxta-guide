#!/bin/sh
mkdir -p server
cp  -f ServerPlatformConfig.master server
java -DJXTA_HOME=server -classpath ../lib/jxta.jar:../lib/log4j.jar:../lib/bcprov-jdk14.jar:. JxtaServerPipeExample
