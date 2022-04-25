TARGET="target/classes"
CLASS_PATH="server.login"
JARS=":json-20211205.jar:Java-WebSocket-1.5.3.jar:slf4j-api-1.7.2.jar:java-jwt-3.19.1.jar:jackson-annotations-2.13.2.jar:jackson-core-2.13.2.jar:jackson-databind-2.13.2.2.jar"
CUR_DIR=$(pwd)

cd "$TARGET"
LOG="${CUR_DIR}/log/login_server_log.txt"
if [ ! -f "$LOG" ]; then
  echo "create login server log file"
  touch "$LOG"
fi
java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".LoginServer 1 2>&1 | tee "$LOG" &