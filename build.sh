#!/bin/sh

TARGET="target"
LOG="log"
JSON_JAR="gson-2.9.0.jar"
CUR_DIR=$(pwd)

if [ ! -d "$TARGET" ]; then
  mkdir $TARGET
fi

if [ ! -d "$LOG" ]; then
  mkdir $LOG
fi

mvn compile

ln -s "$CUR_DIR/src/main/resources/." "$CUR_DIR/$TARGET/classes/"
