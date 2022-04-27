package client.comm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import config.GlobalConfig;
import config.ServerMsg;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;


public class WebSocketHandler extends WebSocketClient {

    public boolean connectionComplete = false;

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

        ServerMsg serverMsg =  new Gson().fromJson(s, ServerMsg.class);

        if (serverMsg.getMsgType() == GlobalConfig.SYSTEM) {

        } else if (serverMsg.getMsgType() == GlobalConfig.CHAT) {

        } else if (serverMsg.getMsgType() == GlobalConfig.INVITATION) {

        } else {

        }

        // success msg {"msgType":2001,"fromUser":"","message":"1000","roomID":0,"timestamp":1650956963695}
        JsonObject jsonObject = new Gson().fromJson(s, JsonObject.class);
        if (jsonObject.has(GlobalConfig.MSG_TYPE))
            if(jsonObject.get(GlobalConfig.MSG_TYPE).getAsInt() == GlobalConfig.SYSTEM)
                if(jsonObject.has(GlobalConfig.MESSAGE))
                    if(jsonObject.get(GlobalConfig.MESSAGE).getAsInt() == GlobalConfig.SUCCESS)
                        this.connectionComplete = true;

    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("Disconnect from ChatServer: " + getURI());
    }

    @Override
    public void onError(Exception e) {
        System.out.println("Exception occurred: " + e);
    }
}
