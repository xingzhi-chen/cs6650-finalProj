package server.config;

import org.json.JSONObject;

/*
* functions for database request body
 */
public class DBReq {
    // keys for JSON serialization/deserialization
    private final String KEY = "key";
    private final String VALUE = "value";
    private final String ACTION = "action";
    private final String APPEND = "append";
    private final String TIMEOUT = "timeout";

    private String key;
    private String value;
    // action code of PUT/GET/DELETE
    private int action;
    private boolean append;
    private int timeout;

    // request body without a value
    public DBReq(String key, int action) {
        this.key = key;
        this.value = null;
        this.action = action;
        this.append = false;
        this.timeout = -1;
    }

    // request body with a value
    public DBReq(String key, String value, int action) {
        this.key = key;
        this.value = value;
        this.action = action;
        this.append = false;
        this.timeout = -1;
    }

    public DBReq(String key, String value, int action, boolean append) {
        this.key = key;
        this.value = value;
        this.action = action;
        this.append = append;
        this.timeout = -1;
    }

    public DBReq(String key, String value, int action, boolean append, int timeout) {
        this.key = key;
        this.value = value;
        this.action = action;
        this.append = append;
        this.timeout = timeout;
    }

    // deserialize the request body from JSON string
    public DBReq(String JSONStr) {
        JSONObject obj = new JSONObject(JSONStr);
        this.key = obj.getString(KEY);
        this.action = obj.getInt(ACTION);
        if (action == ServerConfig.ACTION_PUT) {
            this.value = obj.getString(VALUE);
            this.append = obj.getBoolean(APPEND);
            this.timeout = obj.getInt(TIMEOUT);
        }
    }

    // serialize the request body to JSON string
    public String toJSONString() {
        JSONObject obj = new JSONObject();
        obj.put(KEY, key);
        obj.put(ACTION, action);
        if (action == ServerConfig.ACTION_PUT) {
            obj.put(VALUE, value);
            obj.put(APPEND, append);
            obj.put(TIMEOUT, timeout);
        }
        return obj.toString();
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public int getAction() {
        return action;
    }

    public boolean getAppend() {
        return append;
    }

    public int getTimeout() {
        return timeout;
    }
}
