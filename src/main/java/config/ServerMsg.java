package config;

import org.json.JSONObject;

import java.util.Date;

public class ServerMsg {
    private static final String MSG_TYPE = "msgType";
    private static final String FROM_USER = "fromUser";
    private static final String ROOM_ID = "roomID";
    private static final String MSG = "message";
    private static final String TIMESTAMP = "timestamp";

    private final int msgType;
    private final String fromUser;
    private final int roomID;
    private final String msg;
    private final long timestamp;

    public ServerMsg(int msgType, String fromUser, int roomID, String msg) {
        this.msgType = msgType;
        this.fromUser = fromUser;
        this.roomID = roomID;
        this.msg = msg;
        timestamp = new Date().getTime();
    }

    public ServerMsg(String msgJSONStr) {
        JSONObject obj = new JSONObject(msgJSONStr);
        this.msgType = obj.getInt(MSG_TYPE);
        this.fromUser = obj.getString(FROM_USER);
        this.roomID = obj.getInt(ROOM_ID);
        this.msg = obj.getString(MSG);
        this.timestamp = obj.getLong(TIMESTAMP);
    }

    public String toJSONString() {
        JSONObject obj = new JSONObject();
        obj.put(MSG_TYPE, msgType);
        obj.put(FROM_USER, fromUser);
        obj.put(ROOM_ID, roomID);
        obj.put(MSG, msg);
        obj.put(TIMESTAMP, timestamp);
        return obj.toString();
    }

    public int getMsgType() {
        return msgType;
    }

    public String getFromUser() {
        return fromUser;
    }

    public int getRoomId() {
        return roomID;
    }

    public String getMsg() {
        return msg;
    }
}
