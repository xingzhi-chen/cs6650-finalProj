TARGET="target/classes"
CLASS_PATH="server.route"
JARS=":json-20211205.jar:Java-WebSocket-1.5.3.jar:slf4j-api-1.7.2.jar:java-jwt-3.19.1.jar"
CUR_DIR=$(pwd)
ROUTE_SIZE=2
ROUTE_PID=()
echo "$$"

cd "$TARGET"
for (( i = 1; i <= $ROUTE_SIZE; i++ ))
do
  LOG="${CUR_DIR}/log/route_server${i}_log.txt"
  if [ ! -f "$LOG" ]; then
    echo "create route server log file"
    touch "$LOG"
  fi
  java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".RouteServer "$i" 2>&1 | tee "$LOG" &
  PID=`expr $! - 1`
  IDX=`expr $i - 1`
  ROUTE_PID+=( $PID )
  sleep 0.47
done

while [ 1 ]
do
  for (( i = 1; i <= $ROUTE_SIZE; i++ ))
  do
    IDX=`expr $i - 1`
    PID=${ROUTE_PID[$IDX]}
    PROC_INFO=$(ps -p $PID | awk 'NR==2')
    if [[ $PROC_INFO == "" ]]; then
      java -cp ."$JARS" -Djava.rmi.server.codebase=file:./ "$CLASS_PATH".RouteServer "$i" 2>&1 | tee -a "$LOG" &
      PID=`expr $! - 1`
      ROUTE_PID[$IDX]=$PID
    fi
  done
  sleep 2
done