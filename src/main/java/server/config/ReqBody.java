package server.config;

import com.google.gson.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
//import org.json.JSONException;
//import org.json.JSONObject;

/*
* functions for request body
 */
public class ReqBody {
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
//    private final GsonBuilder builder = new GsonBuilder();


    // request body without a value
    public ReqBody(String key, int action) {
        this.key = key;
        this.value = null;
        this.action = action;
        this.append = false;
        this.timeout = -1;
    }

    // request body with a value
    public ReqBody(String key, String value, int action) {
        this.key = key;
        this.value = value;
        this.action = action;
        this.append = false;
        this.timeout = -1;
    }

    public ReqBody(String key, String value, int action, boolean append) {
        this.key = key;
        this.value = value;
        this.action = action;
        this.append = append;
        this.timeout = -1;
    }

    public ReqBody(String key, String value, int action, boolean append, int timeout) {
        this.key = key;
        this.value = value;
        this.action = action;
        this.append = append;
        this.timeout = timeout;
    }

    // deserialize the request body from JSON string
    public ReqBody(String JSONStr) {
        JSONObject obj = new JSONObject(JSONStr);
        this.key = obj.getString(KEY);
        this.action = obj.getInt(ACTION);
        if (action == ServerConfig.ACTION_PUT) {
            this.value = obj.getString(VALUE);
            this.append = obj.getBoolean(APPEND);
            this.timeout = obj.getInt(TIMEOUT);
        }
//        builder.serializeNulls();
//        Gson gson = builder.create();
//        ReqBody reqBody = gson.fromJson(JSONStr, ReqBody.class);
//        this.key = reqBody.getKey();
//        this.value = reqBody.getValue();
//        this.action = reqBody.getAction();
//        this.append = reqBody.getAppend();
//        this.timeout = reqBody.getTimeout();
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
//        builder.serializeNulls();
//        Gson gson = builder.create();
//        return gson.toJson(this);
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
