#!/bin/sh
mkdir -p client
cp -f PlatformConfig.master client/PlatformConfig
java -DRDVWAIT=false -DJXTA_HOME=client -classpath ../lib/jxta.jar:../lib/log4j.jar:../lib/bcprov-jdk14.jar:. JxtaSocketExample
