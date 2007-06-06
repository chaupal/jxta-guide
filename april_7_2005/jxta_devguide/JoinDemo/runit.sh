#!/bin/sh
java -DJXTA_HOME=client -classpath ../lib/jxta.jar:../lib/log4j.jar:../lib/bcprov-jdk14.jar:. JoinDemo
