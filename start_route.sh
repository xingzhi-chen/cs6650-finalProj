TARGET="target/classes"
CLASS_PATH="server.route"
JSON_JAR="json-20211205.jar"
CUR_DIR=$(pwd)

cd "$TARGET"
LOG="${CUR_DIR}/log/route_server_log.txt"
if [ ! -f "$LOG" ]; then
  echo "create route server log file"
  touch "$LOG"
fi
java -cp .:"$JSON_JAR" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".RouteServer 2>&1 | tee "$LOG" &