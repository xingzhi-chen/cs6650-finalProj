package server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;
//import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
* functions for response body
 */
public class RspBody {
    private final String RES_CODE = "resCode";
    private final String VALUE = "value";
    private final String MESSAGE = "message";

    // result code from the server
    private final int resCode;
    // value if the request is a GET request
    private final ArrayList<String> value;
    // detailed result message from the server
    private final String message;
//    private final GsonBuilder builder = new GsonBuilder();

    // response body without a value
    public RspBody(int resCode, String message) {
        this.resCode = resCode;
        this.value = null;
        this.message = message;
    }

    // response body with a value
    public RspBody(int resCode, ArrayList<String> value, String message) {
        this.resCode = resCode;
        this.value = value;
        this.message = message;
    }

    // parse the response body from a JSON string
    public RspBody(String JSONStr) {
        JSONObject obj = new JSONObject(JSONStr);
        resCode = obj.getInt(RES_CODE);
        message = obj.getString(MESSAGE);
        if (obj.has(VALUE)) {
            value = new ArrayList<>();
            for (Object val : obj.getJSONArray(VALUE)) {
                value.add(val.toString());
            }
        } else {
            value = null;
        }
//        builder.serializeNulls();
//        Gson gson = builder.create();
//        RspBody rspBody = gson.fromJson(JSONStr, RspBody.class);
//        this.resCode = rspBody.getResCode();
//        this.value = rspBody.getValue();
//        this.message = rspBody.getMessage();
    }

    // serialize the response body to a JSON string
    public String toJSONString() {
        JSONObject obj = new JSONObject();
        obj.put(RES_CODE, resCode);
        obj.put(MESSAGE, message);
        if (value != null) {
            obj.put(VALUE, value);
        }
        return obj.toString();
//        builder.serializeNulls();
//        Gson gson = builder.create();
//        return gson.toJson(this);
    }

    // helper function for log
    public String toString() {
        String res = "";
        res += "resCode: " + resCode + ", ";
        res += "message: " + message;
        if (value != null) {
            res += ", value: " + value.toString();
        }
        return res;
    }

    public int getResCode() {
        return resCode;
    }

    public ArrayList<String> getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
