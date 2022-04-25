#!/bin/sh

TARGET="target/classes"
CLASS_PATH="server.database"
JSON_JAR="json-20211205.jar"
CUR_DIR=$(pwd)

ACCEPTOR_SIZE=5
PROPOSER_SIZE=2
LEARNER_SIZE=2

if [ ! -d "$TARGET" ]; then
  echo 'target client program does not exist, call build.sh'
  ./build.sh
fi

cd "$TARGET"

# start proposers with learners
for (( i = 1; i <= $PROPOSER_SIZE; i++ ))
do
  LOG="${CUR_DIR}/log/proposer_${i}_log.txt"
  if [ ! -f "$LOG" ]; then
    echo "create proposer ${i} log file"
    touch "$LOG"
  fi
  java -cp .:"$JSON_JAR" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".Proposer "$i" &>"$LOG" &
  echo start proposer "$i" "$port"
done

# start the rest of learners
for (( i = $LEARNER_SIZE; i > $PROPOSER_SIZE; i-- ))
do
  LOG="${CUR_DIR}/log/learner_${i}_log.txt"
  if [ ! -f "$LOG" ]; then
    echo "create learner ${i} log file"
    touch "$LOG"
  fi
  java -cp .:"$JSON_JAR" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".Learner "$i" &>"$LOG" &
  echo start learner "$i" "$port"
done

# start acceptors
for (( i = 1; i <= $ACCEPTOR_SIZE; i++ ))
do
  LOG="${CUR_DIR}/log/acceptor_${i}_log.txt"
  if [ ! -f "$LOG" ]; then
    echo "create acceptor ${i} log file"
    touch "$LOG"
  fi
  java -cp .:"$JSON_JAR" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".Acceptor "$i" &>"$LOG" &
  echo start acceptor "$i" "$port"
done