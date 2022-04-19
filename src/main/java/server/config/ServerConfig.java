package server.config;

import java.util.HashMap;

/*
* Configurations for DB server and other servers
 */
public class ServerConfig {
    // server registration prefix for rmiregistry, server 1 will be registered as RMIDBServer1
    public static final String RPC_DB_NAME = "RMIDBServer";
    public static final int CLUSTER_SIZE = 2;

    // timeout for server-server response inside cluster, 1 seconds
    public static final int CLUSTER_TIMEOUT = 1000;

    // codes for PUT/GET/DELETE actions
    public static final int ACTION_PUT = 1001;
    public static final int ACTION_GET = 1002;
    public static final int ACTION_DELETE = 1003;

    // codes for the server results
    public static final int SUCCESS = 2000;
    public static final int ERROR_NO_EXIST = 2001;
    public static final int ERROR_NO_ACTION = 2002;
    public static final int SERVER_ERROR = 2003;
    public static final int REQ_OUTDATED = 2004;

    // default messages for result codes
    public static final HashMap<Integer, String> errorMsg = new HashMap<>() {{
        put(SUCCESS, "Success");
        put(ERROR_NO_EXIST, "Requested key does not exist");
        put(ERROR_NO_ACTION, "Invalid requested action");
        put(SERVER_ERROR, "Exceptions at the server");
        put(REQ_OUTDATED, "request data is outdated, denied by server");
    }};
}
