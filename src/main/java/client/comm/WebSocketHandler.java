package client.comm;

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
        System.out.println("Connected to ChatServer: " + getURI());
    }

    @Override
    public void onMessage(String s) {
        System.out.println("new msg: " + s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("Disconnect from ChatServer: " + getURI());
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        System.out.println("Exception occurred: " + e);
    }
}
