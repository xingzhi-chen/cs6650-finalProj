package config;

import org.json.JSONObject;

public class ServerMsg {
    private static final String MSG_TYPE = "msgType";
    private static final String FROM_USER = "fromUser";
    private static final String ROOM_ID = "roomID";
    private static final String MSG = "message";

    private final int msgType;
    private final String fromUser;
    private final int roomID;
    private final String msg;

    public ServerMsg(int msgType, String fromUser, int roomID, String msg) {
        this.msgType = msgType;
        this.fromUser = fromUser;
        this.roomID = roomID;
        this.msg = msg;
    }

    public ServerMsg(String msgJSONStr) {
//        builder.serializeNulls();
//        Gson gson = builder.create();
//        ServerMsg serverMsg = gson.fromJson(msgJSONStr, ServerMsg.class);
//        this.msgType = serverMsg.getMsgType();
//        this.fromUser = serverMsg.getFromUser();
//        this.roomID = serverMsg.getRoomId();
//        this.msg = serverMsg.getMsg();
        JSONObject obj = new JSONObject(msgJSONStr);
        this.msgType = obj.getInt(MSG_TYPE);
        this.fromUser = obj.getString(FROM_USER);
        this.roomID = obj.getInt(ROOM_ID);
        this.msg = obj.getString(MSG);
    }

    public String toJSONString() {
//        builder.serializeNulls();
//        Gson gson = builder.create();
//        return gson.toJson(this);
        JSONObject obj = new JSONObject();
        obj.put(MSG_TYPE, msgType);
        obj.put(FROM_USER, fromUser);
        obj.put(ROOM_ID, roomID);
        obj.put(MSG, msg);
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
