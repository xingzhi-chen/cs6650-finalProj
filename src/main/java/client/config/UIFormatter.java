package client.config;

import config.ServerMsg;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UIFormatter {

    public static List<String> formatChat(List<ServerMsg> msgList) {
        List<String> list = new ArrayList<>();
        for (ServerMsg msg: msgList) {
            list.add(serverMsgToChatMsg(msg));
        }
        return list;
    }

    public static String serverMsgToChatMsg(ServerMsg msg) {
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        String d = format.format(new Date(msg.getTimestamp()));
        return String.format("[%s] %s: %s", d, msg.getFromUser(), msg.getMsg());
    }

    public static List<String> formatInvitedRoom(List<ServerMsg> msgList) {
        List<String> list = new ArrayList<>();
        for (ServerMsg msg: msgList) {
            list.add(serverMsgToInvitedRoomMsg(msg));
        }
        return list;
    }

    public static String serverMsgToInvitedRoomMsg(ServerMsg msg){
        return String.format("New Invite! RoomID: %s (From user: %s)}", msg.getRoomId(), msg.getFromUser());
    }

    public static List<String> formatAvailableRoom(List<Integer> roomIDList) {
        List<String> list = new ArrayList<>();
        for (int roomID: roomIDList) {
            list.add(roomIDToAvailableRoom(roomID));
        }
        return list;
    }

    public static String roomIDToAvailableRoom(int roomID){
        return String.format("RoomID: %s", roomID);
    }
}
