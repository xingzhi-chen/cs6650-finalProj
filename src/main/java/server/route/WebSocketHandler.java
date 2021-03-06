package server.route;

import config.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;
import server.config.DBHelper;
import server.config.ServerConfig;
import server.config.ServerHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/*
* Handler for websocket connection
* */
public class WebSocketHandler extends WebSocketServer {
    // HashMap of username to websocket
    private final HashMap<String, WebSocket> sockets;
    private final HashSet<String> registeredUsers;
    private final DBHelper dbHelper;

    public WebSocketHandler(DBHelper dbHelper, int port) {
        super(new InetSocketAddress(port));
        sockets = new HashMap<>();
        registeredUsers = new HashSet<>();
        this.dbHelper = dbHelper;
    }

    // send server message to user through the stored websocket
    public void sendMsgToClient(String toUser, String msg) throws IOException {
        if (sockets.containsKey(toUser)) {
            if (sockets.get(toUser).isOpen()) {
                sockets.get(toUser).send(msg);
                return;
            } else {
                sockets.remove(toUser);
            }
        }
        if (registeredUsers.contains(toUser) || dbHelper.checkUsername(toUser) == ServerConfig.SUCCESS) {
            registeredUsers.add(toUser);
            ServerMsg serverMsg = new ServerMsg(msg);
            // only resend invitation msg, chat msg can be got through /get_chat_history api
            if (serverMsg.getMsgType() == GlobalConfig.INVITATION) {
                dbHelper.saveUnsentMsg(toUser, msg);
            }
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        Log.Debug("new client come in");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {

    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        // the only message client will send to server is token
        JSONObject obj = new JSONObject(s);
        // send the token check result through ServerMsg.msg
        if (!obj.has("token")) {
            webSocket.send(new ServerMsg(GlobalConfig.SYSTEM, "", 0, String.valueOf(GlobalConfig.MISSING_ARGS)).toJSONString());
            webSocket.close();
        }

        String username = ServerHelper.parseToken(obj.getString("token"));
        int dbResCode = dbHelper.checkUsername(username);
        if (dbResCode == ServerConfig.ERROR_NO_EXIST) {
            webSocket.send(new ServerMsg(GlobalConfig.SYSTEM, "", 0, String.valueOf(GlobalConfig.NO_MATCH)).toJSONString());
            webSocket.close();
            return;
        } else if (dbResCode == ServerConfig.SERVER_ERROR) {
            webSocket.send(new ServerMsg(GlobalConfig.SYSTEM, "", 0, String.valueOf(GlobalConfig.SERVER_ERROR)).toJSONString());
            webSocket.close();
            return;
        }

        webSocket.send(new ServerMsg(GlobalConfig.SYSTEM, "", 0, String.valueOf(GlobalConfig.SUCCESS)).toJSONString());
        ArrayList<String> prevMsgs = dbHelper.getUnsentMsg(username);
        if (prevMsgs != null) {
            for (String msg : prevMsgs) {
                webSocket.send(msg);
            }
        }
        dbHelper.clearSavedMsg(username);
        // add socket-user info to map
        sockets.put(username, webSocket);
        registeredUsers.add(username);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        Log.Error("websocket error: ");
        e.printStackTrace();
    }

    @Override
    public void onStart() {
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }
}
