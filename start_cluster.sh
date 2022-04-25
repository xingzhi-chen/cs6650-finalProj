#!/bin/sh

TARGET="target/classes"
JARS=":json-20211205.jar:Java-WebSocket-1.5.3.jar:slf4j-api-1.7.2.jar:java-jwt-3.19.1.jar"
CUR_DIR=$(pwd)

ACCEPTOR_SIZE=5
PROPOSER_SIZE=2
LEARNER_SIZE=2
ACCEPTOR_PID=()
PROPOSER_PID=()
LEARNER_PID=()

ROOM_SERVER_SIZE=3
ROOM_PID=()

ROUTE_SIZE=2
ROUTE_PID=()

LOGIN_SIZE=2
LOGIN_PID=()

if [ ! -d "$TARGET" ]; then
  echo 'target server program does not exist, call build.sh'
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
  java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.database.Proposer "$i" &>"$LOG" &
  PID=`expr $! - 1`
  PROPOSER_PID+=( $PID )
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
  java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.database.Learner "$i" &>"$LOG" &
  PID=`expr $! - 1`
  LEARNER_PID+=( $PID )
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
  java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.database.Acceptor "$i" &>"$LOG" &
  PID=`expr $! - 1`
  ACCEPTOR_PID+=( $PID )
  echo start acceptor "$i" "$port"
done

sleep 3
# start RoomServers
for (( i = 1; i <= ROOM_SERVER_SIZE; i++ ))
do
  LOG="${CUR_DIR}/log/room_server_${i}_log.txt"
    if [ ! -f "$LOG" ]; then
      echo "create room server ${i} log file"
      touch "$LOG"
    fi
  java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.room.RoomServer "$i" 2>&1 | tee "$LOG" &
  PID=`expr $! - 1`
  ROOM_PID+=( $PID )
  echo start room server "$i"
done

# start RouteServers
for (( i = 1; i <= $ROUTE_SIZE; i++ ))
do
  LOG="${CUR_DIR}/log/route_server${i}_log.txt"
  if [ ! -f "$LOG" ]; then
    echo "create route server log file"
    touch "$LOG"
  fi
  java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.route.RouteServer "$i" 2>&1 | tee "$LOG" &
  PID=`expr $! - 1`
  ROUTE_PID+=( $PID )
  sleep 0.47
done

# start LoginServers
for (( i = 1; i <= $LOGIN_SIZE; i++ ))
do
  LOG="${CUR_DIR}/log/login_server${i}_log.txt"
  if [ ! -f "$LOG" ]; then
    echo "create login server log file"
    touch "$LOG"
  fi
  java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.login.LoginServer "$i" 2>&1 | tee "$LOG" &
  PID=`expr $! - 1`
  LOGIN_PID+=( $PID )
  echo start login server "$i"
done

while [ 1 ]
do
  for (( i = 1; i <= $PROPOSER_SIZE; i++ ))
  do
    IDX=`expr $i - 1`
    PID=${PROPOSER_PID[$IDX]}
    PROC_INFO=$(ps -p $PID | awk 'NR==2')
    if [[ $PROC_INFO == "" ]]; then
      java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.database.Proposer "$i" &>"$LOG" &
      PID=`expr $! - 1`
      PROPOSER_PID[$IDX]=$PID
    fi
  done
  for (( i = $LEARNER_SIZE; i > $PROPOSER_SIZE; i-- ))
  do
    IDX=`expr $LEARNER_SIZE - $i`
    PID=${LEARNER_PID[$IDX]}
    PROC_INFO=$(ps -p $PID | awk 'NR==2')
    if [[ $PROC_INFO == "" ]]; then
      java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.database.Learner "$i" &>"$LOG" &
      PID=`expr $! - 1`
      LEARNER_PID[$IDX]=$PID
    fi
  done
  for (( i = 1; i <= $ACCEPTOR_SIZE; i++ ))
  do
    IDX=`expr $i - 1`
    PID=${ACCEPTOR_PID[$IDX]}
    PROC_INFO=$(ps -p $PID | awk 'NR==2')
    if [[ $PROC_INFO == "" ]]; then
      java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.database.Acceptor "$i" &>"$LOG" &
      PID=`expr $! - 1`
      ACCEPTOR_PID[$IDX]=$PID
    fi
  done
  for (( i = 1; i <= $ROOM_SERVER_SIZE; i++ ))
  do
    IDX=`expr $i - 1`
    PID=${ROOM_PID[$IDX]}
    PROC_INFO=$(ps -p $PID | awk 'NR==2')
    if [[ $PROC_INFO == "" ]]; then
      java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.room.RoomServer "$i" 2>&1 | tee -a "$LOG" &
      PID=`expr $! - 1`
      ROOM_PID[$IDX]=$PID
    fi
  done
  for (( i = 1; i <= $ROUTE_SIZE; i++ ))
  do
    IDX=`expr $i - 1`
    PID=${ROUTE_PID[$IDX]}
    PROC_INFO=$(ps -p $PID | awk 'NR==2')
    if [[ $PROC_INFO == "" ]]; then
      java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.route.RouteServer "$i" 2>&1 | tee -a "$LOG" &
      PID=`expr $! - 1`
      ROUTE_PID[$IDX]=$PID
    fi
  done
  for (( i = 1; i <= $LOGIN_SIZE; i++ ))
  do
    IDX=`expr $i - 1`
    PID=${LOGIN_PID[$IDX]}
    PROC_INFO=$(ps -p $PID | awk 'NR==2')
    if [[ $PROC_INFO == "" ]]; then
      java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ server.login.LoginServer "$i" 2>&1 | tee -a "$LOG" &
      PID=`expr $! - 1`
      LOGIN_PID[$IDX]=$PID
    fi
  done
  sleep 2
done