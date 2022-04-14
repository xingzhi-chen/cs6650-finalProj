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

    // resCode for server response to client
    public static final int SUCCESS = 1000;
    public static final int NETWORK_ERROR = 1001;
    public static final int DUP_USERNAME = 1002;
    public static final int NO_MATCH = 1003;

    public static final HashMap<Integer, String> errorMsg = new HashMap<>() {{
        put(SUCCESS, "success");
        put(NETWORK_ERROR, "internal server not available");
        put(DUP_USERNAME, "username already exists");
        put(NO_MATCH, "username or password does not match record");
    }};

    // code identifying message type from server
    public static final int CHAT = 2001;
    public static final int INVITATION = 2002;
}
