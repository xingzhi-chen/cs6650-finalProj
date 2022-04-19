package server.database.config;

import server.config.ServerConfig;

import java.util.HashMap;

/*
* Configurations within DB servers
 */
public class DBConfig {
    // number of acceptors/proposers/learners in the cluster
    public static final int ACCEPTOR_SIZE = 5;
    public static final int PROPOSER_SIZE = ServerConfig.CLUSTER_SIZE;
    public static final int LEARNER_SIZE = 2;
    // majority amount
    public static final int QUORUM = ACCEPTOR_SIZE / 2 + 1;
    // server registration prefix for rmiregistry, server 1 will be registered as RMIDBServer1
    public static final String RPC_DB_NAME = ServerConfig.RPC_DB_NAME;
    public static final String RPC_ACCEPTOR_NAME = "RMIAcceptor";
    public static final String RPC_LEARNER_NAME = "RMILearner";

    // timeout for server-server response inside the DB cluster, 0.5 seconds
    public static final int DB_CLUSTER_TIMEOUT = 500;

    // codes for PAXOS processes
    public static final int PROMISED = 3000;
    public static final int DENIED = 3001;
    public static final int ACCEPTED = 3002;
}
