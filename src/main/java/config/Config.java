package config;

import java.util.HashMap;

/*
* Configurations for both client and server
 */
public class Config {
    // keys for JSON serialization/deserialization

    // number of servers in the cluster
    public static final int CLUSTER_SIZE = 5;

    // timeout for client-server response, 10 seconds
    public static final int SERVER_TIMEOUT = 10000;
    // timeout for server-server response inside cluster, 1 seconds
    public static final int CLUSTER_TIMEOUT = 1000;

    // code identifying message type from server
    public static final int CHAT = 2001;
    public static final int INVITATION = 2002;
}
