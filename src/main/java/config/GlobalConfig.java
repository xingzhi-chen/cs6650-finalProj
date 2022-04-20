package config;

import org.json.JSONObject;

import java.util.HashMap;

/*
* Configurations for both client and server
 */
public class GlobalConfig {
    // timeout for client-server response, 10 seconds
    public static final int SERVER_TIMEOUT = 10000;

    public static final String RES_CODE = "resCode";
    public static final String MESSAGE = "message";
    public static final String ROOM_ID = "roomID";
    public static final String TOKEN = "token";
    public static final String NEW_USER = "newUser";
    public static final String ACCEPT = "accept";

    // resCode for server response to client
    public static final int SUCCESS = 1000;
    public static final int SERVER_ERROR = 1001;
    public static final int DUP_USERNAME = 1002;
    public static final int NO_MATCH = 1003;
    public static final int MISSING_ARGS = 1004;
    public static final int NO_ROOM = 1005;

    public static final HashMap<Integer, String> errorMsg = new HashMap<>() {{
        put(SUCCESS, "success");
        put(SERVER_ERROR, "internal server not available");
        put(DUP_USERNAME, "username already exists");
        put(NO_MATCH, "username or password does not match record");
        put(MISSING_ARGS, "missing request argument(s)");
        put(NO_MATCH, "no such a room in the system");
    }};

    // code identifying message type from server
    public static final int CHAT = 2001;
    public static final int INVITATION = 2002;
}
