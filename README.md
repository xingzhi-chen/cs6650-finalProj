# CS6650 Final Project by Group 7
# Xingzhi Chen, Siyu Chen, Chia-Wei Lin
# Project about a Chat room system

1. Start the servers:
   1. There are 4 parts in the server cluster: database, route, login service, room service. Each part contains multiple servers
   2. First, use './run_rmiregistry.sh' to start the RMI registry for internal service
   3. Second, use './start_cluster.sh' to start all servers in the 4 parts. The bash program then is responsible for keep all server processes alive, so do not close the terminal running the bash script.
   4. Ports 8080, 8081, 8082, 8083, 8090, 8091 may be used for HTTP listening, please make sure these ports are available.
2. Start the client:
   1. After starting the server, use './start_client.sh' to start a client program
   2. To open multiple user interface, just run './start_client.sh' again
3. Stop the servers:
   1. Press 'Ctrl+C' in the terminal running the start_cluster.sh script, which will stop the bash program
   2. Use './stop_cluster.sh' to stop all servers running background