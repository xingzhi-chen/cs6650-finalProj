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
import java.util.HashMap;

public class WebSocketHandler extends WebSocketServer {
    private final HashMap<String, WebSocket> sockets;
    private final HashMap<String, String> remoteAddrs;
    private final DBHelper dbHelper;

    public WebSocketHandler(DBHelper dbHelper, int port) {
        super(new InetSocketAddress(port));
        sockets = new HashMap<>();
        remoteAddrs = new HashMap<>();
        this.dbHelper = dbHelper;
    }

    public void sendMsgToClient(String toUser, String msg) throws IOException {
        if (sockets.containsKey(toUser) && sockets.get(toUser).isOpen()) {
            sockets.get(toUser).send(msg);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        Log.Debug("new client come in");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        String addr = webSocket.getRemoteSocketAddress().toString();
        if (remoteAddrs.containsKey(addr)) {
            String username = remoteAddrs.get(addr);
            sockets.remove(username);
            Log.Debug("client %s left", username);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println("new msg: " + s);
        JSONObject obj = new JSONObject(s);
        if (!obj.has("token")) {
            webSocket.send(new ServerMsg(GlobalConfig.SYSTEM, "", 0, GlobalConfig.errorMsg.get(GlobalConfig.MISSING_ARGS)).toJSONString());
            webSocket.close();
        }

        String username = ServerHelper.parseToken(obj.getString("token"));
        int dbResCode = dbHelper.checkUsername(username);
        if (dbResCode == ServerConfig.ERROR_NO_EXIST) {
            webSocket.send(new ServerMsg(GlobalConfig.SYSTEM, "", 0, GlobalConfig.errorMsg.get(GlobalConfig.NO_MATCH)).toJSONString());
            webSocket.close();
            return;
        } else if (dbResCode == ServerConfig.SERVER_ERROR) {
            webSocket.send(new ServerMsg(GlobalConfig.SYSTEM, "", 0, GlobalConfig.errorMsg.get(GlobalConfig.SERVER_ERROR)).toJSONString());
            webSocket.close();
            return;
        }

        sockets.put(username, webSocket);
        remoteAddrs.put(webSocket.getRemoteSocketAddress().toString(), username);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }

    @Override
    public void onStart() {
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }
}
