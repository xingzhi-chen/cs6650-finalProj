TARGET="target/classes"
CLASS_PATH="server.route"
JARS=":json-20211205.jar:Java-WebSocket-1.5.3.jar:slf4j-api-1.7.2.jar"
CUR_DIR=$(pwd)

LOG1="${CUR_DIR}/log/route_server1_log.txt"
if [ ! -f "$LOG1" ]; then
  echo "create route server log file"
  touch "$LOG1"
fi
LOG2="${CUR_DIR}/log/route_server2_log.txt"
if [ ! -f "$LOG2" ]; then
  echo "create route server log file"
  touch "$LOG2"
fi

cd "$TARGET"
java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".RouteServer 1 2>&1 | tee "$LOG1" &
sleep 0.47
java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".RouteServer 2 2>&1 | tee "$LOG2" &