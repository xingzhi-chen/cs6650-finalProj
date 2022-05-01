package client.comm;

import config.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;


public class WebSocketHandler extends WebSocketClient {

    public boolean connected = false;

    public WebSocketHandler(URI uri){
        super(uri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Log.Info("Connected to ChatServer: " + getURI());
    }

    @Override
    public void onMessage(String s) {
        Log.Info("new msg: " + s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        Log.Info("Disconnect from ChatServer: " + getURI());
    }

    @Override
    public void onError(Exception e) {
        Log.Error("Exception occurred: " + e);
    }
}
