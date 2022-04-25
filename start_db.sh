#!/bin/sh

TARGET="target/classes"
CLASS_PATH="server.database"
JARS=":json-20211205.jar:Java-WebSocket-1.5.3.jar:slf4j-api-1.7.2.jar:java-jwt-3.19.1.jar:jackson-annotations-2.13.2.jar:jackson-core-2.13.2.jar:jackson-databind-2.13.2.2.jar"
CUR_DIR=$(pwd)

ACCEPTOR_SIZE=3
PROPOSER_SIZE=1
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
  java -cp .:"$JARS" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".Proposer "$i" &>"$LOG" &
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
  java -cp .:"$JARS" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".Learner "$i" &>"$LOG" &
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
  java -cp .:"$JARS" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".Acceptor "$i" &>"$LOG" &
  echo start acceptor "$i" "$port"
done