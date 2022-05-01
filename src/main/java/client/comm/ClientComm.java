package client.comm;


import client.config.ClientHelper;
import client.config.RequestFailureException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import config.GlobalConfig;
import config.Log;
import config.ServerMsg;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ClientComm implements ClientCommInterface{

    protected String token;
    protected String username;
    protected List<ServerMsg> invitedList;
    protected List<Integer> availableRoomList;
    protected HashMap<Integer, ArrayList<ServerMsg>> chatHistory;
    protected String clientMsg;
    protected Integer wsPort;
    protected Integer routePort;

    protected WebSocketHandler webSocketHandler;

    public ClientComm() {
        initComm();
    }

    public void initComm() {
        this.token = "";
        this.username = "";
        this.availableRoomList = new ArrayList<>();
        this.invitedList = new ArrayList<>();
        this.chatHistory = new HashMap<>();
        this.webSocketHandler = null;
    }

    public static void main(String[] args) {
        ClientComm comm = new ClientComm();

        System.out.println("\n==Test register==");
        comm.register("user", "password");

        System.out.println("\n==Test login==");
        comm.login("user", "password");

        System.out.println("\n==Test ws");
        comm.websocketConnection(comm.token);

        System.out.println("\n==Test createRoom");
        comm.createRoom(comm.token);

//        System.out.println("\n==Test inv");
//        comm.sendInvitation(comm.getToken(), "user", 8928);
//
//        System.out.println("\n==Test send msg");
//        comm.sendMessage(comm.getToken(), "XXXXXXXXX", 8928);
//
//        System.out.println("====");
//        System.out.println(comm.chatHistory);

    }


    @Override
    public void register(String username, String password) {

        for (int port: GlobalConfig.LOGIN_PORTS){

            // set request
            String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
            HttpRequest request = ClientHelper.parseRegisterRequest(host, username, password);
            Log.Info("Send register request to server=" + host);

            try {
                // get response
                JsonObject jsonObject = ClientHelper.getResponseValue(request);

                // validate response
                if (!isValid(jsonObject))
                    throw new RequestFailureException("Server response error" + jsonObject);

                // process valid response
                if (isSucceed(jsonObject)) {}
                this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                return;

            } catch (RequestFailureException re){
                Log.Error("Error=" + re.getMessage());
                continue;   // try next port if failed
            }
        }
        this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        return;
    }

    @Override
    public void login(String username, String password) {
        for (int port: GlobalConfig.LOGIN_PORTS){

            //set request
            String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
            HttpRequest request = ClientHelper.parseLoginRequest(host, username, password);
            Log.Info("Send login request to server=" + host);
            try {
                // get response
                JsonObject jsonObject = ClientHelper.getResponseValue(request);

                // validate response
                if (!isValid(jsonObject))
                    throw new RequestFailureException("Server response error" + jsonObject);

                // process valid response
                if (isSucceed(jsonObject) && jsonObject.has(GlobalConfig.TOKEN) && jsonObject.has(GlobalConfig.ROOM_LIST))  {
                    // store username
                    this.username = username;
                    // store token
                    this.token = jsonObject.get(GlobalConfig.TOKEN).getAsString();
                    // retrieve room list
                    JsonArray jsonArray = jsonObject.get(GlobalConfig.ROOM_LIST).getAsJsonArray();
                    if (jsonArray != null) {
                        for (JsonElement element : jsonArray)
                            this.availableRoomList.add(element.getAsInt());
                    }
                }
                this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                return;

            } catch (RequestFailureException re){
                Log.Error("Error=" + re.getMessage());
                continue;   // try next port if failed
            }
        }
        this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        return;
    }

    @Override
    public void websocketConnection(String token) {

        if (token == null || token.isEmpty()){
            this.clientMsg = "Cannot connect to server.";
            return;
        }

        for (int i=0; i<GlobalConfig.WEBSOCKET_PORTS.size(); i++) { // Try different websocket ports
            int port = GlobalConfig.WEBSOCKET_PORTS.get(i);
            String host = String.format("ws://%s:%s", GlobalConfig.IP_ADDRESS, port);
            webSocketHandler = new WebSocketHandler(URI.create(host)) {
                @Override
                public void onMessage(String s) {
                    Log.Info("msg=" + s);
                    ServerMsg serverMsg = new ServerMsg(s);
                    int msgType = serverMsg.getMsgType();
                    switch (msgType) {
                        case GlobalConfig.SYSTEM:
                            clientMsg = GlobalConfig.errorMsg.get(Integer.valueOf(serverMsg.getMsg()));
                            if (Integer.valueOf(serverMsg.getMsg()).equals(GlobalConfig.SUCCESS))
                                this.connected = true;
                            break;
                        case GlobalConfig.CHAT:
                            chatHistory.get(serverMsg.getRoomId()).add(serverMsg);
                            break;
                        case GlobalConfig.INVITATION:
                            invitedList.add(serverMsg);
                            break;
                        default:
                            clientMsg = "Cannot resolve websocket message from server.";
                    }
                }
            };

            try {
                webSocketHandler.connectBlocking(GlobalConfig.SERVER_TIMEOUT, MILLISECONDS);
                Log.Info("Verify token..." + token);
                webSocketHandler.send(new JSONObject().put(GlobalConfig.TOKEN, token).toString());
                Thread.sleep(1000);

                if (this.webSocketHandler.connected) {

                    // save ws port number
                    this.wsPort = GlobalConfig.WEBSOCKET_PORTS.get(i);
                    this.routePort = GlobalConfig.ROUTE_PORTS.get(i);

                    // get room list and chat history
                    for (int roomID : this.availableRoomList) {
                        getHistory(this.token, roomID);
                        this.clientMsg = "Welcome to the chat room";
                    }
                    return;
                }
            } catch (InterruptedException e) {
                Log.Error("Error occurs while verifying the token: " + e);
                this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
                continue;
            }
        }
    }

    @Override
    public void createRoom(String token) {
        for (int port: GlobalConfig.ROUTE_PORTS){

            // set response
            String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
            HttpRequest request = ClientHelper.parseCreateRoomRequest(host, token);
            Log.Info("Send create room request to server=" + host);

            try {
                // get response
                JsonObject jsonObject = ClientHelper.getResponseValue(request);

                // validate response
                if (!isValid(jsonObject))
                    throw new RequestFailureException("Server response error" + jsonObject);

                // process valid response
                if (isSucceed(jsonObject)  && jsonObject.has(GlobalConfig.ROOM_ID)) {
                    int roomID = jsonObject.get(GlobalConfig.ROOM_ID).getAsInt();
                    this.availableRoomList.add(roomID);
                    this.chatHistory.put(roomID, new ArrayList<>());
                }

                this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                return;

            } catch (RequestFailureException re){
                Log.Error("Error=" + re.getMessage());
                continue;   // try next port if failed
            }
        }
        this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        return;
    }

    @Override
    public void sendInvitation(String token, String otherUsername, int roomID) {
        for (int port: GlobalConfig.ROUTE_PORTS){

            // set request
            String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
            HttpRequest request = ClientHelper.parseSendInviteRequest(host, token, otherUsername, roomID);
            Log.Info("Send invitation request to server=" + host);

            try {
                // get response
                JsonObject jsonObject = ClientHelper.getResponseValue(request);

                // validate response
                if (!isValid(jsonObject))
                    throw new RequestFailureException("Server response error" + jsonObject);

                // process valid response
                if (isSucceed(jsonObject)) {}

                this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                return;

            } catch (RequestFailureException re){
                Log.Error("Error=" + re.getMessage());
                continue;   // try next port if failed
            }
        }
        this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        return;
    }

    @Override
    public void sendInvitationRsp(String token, int roomID, boolean accept) {
        for (int port: GlobalConfig.ROUTE_PORTS){

            // set request
            String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
            HttpRequest request = ClientHelper.parseSendInviteRspRequest(host, token, roomID, accept);
            Log.Info("Send invitation response to server=" + host);

            try {
                // get response
                JsonObject jsonObject = ClientHelper.getResponseValue(request);

                // validate response
                if (!isValid(jsonObject))
                    throw new RequestFailureException("Server response error" + jsonObject);

                // process valid response
                if (isSucceed(jsonObject)) {

                    // update invited list
                    List<ServerMsg> serverMsgList = new ArrayList<>();
                    for (ServerMsg msg: invitedList) {
                        if (msg.getRoomId() != roomID) {
                            serverMsgList.add(msg);
                        }
                    }
                    invitedList = serverMsgList;

                    // update chat history
                    availableRoomList.add(roomID);
                    getHistory(this.token, roomID);
                }

                this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                return;

            } catch (RequestFailureException re){
                Log.Error("Error=" + re.getMessage());
                continue;   // try next port if failed
            }
        }
        this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        return;
    }

    @Override
    public void sendMessage(String token, String message, int roomID) {
        for (int port: GlobalConfig.ROUTE_PORTS){

            //set request
            String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
            HttpRequest request = ClientHelper.parseSendMsgRequest(host, token, message, roomID);
            Log.Info("Send send message request to server=" + host);

            try {
                // get response
                JsonObject jsonObject = ClientHelper.getResponseValue(request);

                // validate response
                if (!isValid(jsonObject))
                    throw new RequestFailureException("Server response error" + jsonObject);

                // process valid response
                if (isSucceed(jsonObject)) {}
                this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                return;

            } catch (RequestFailureException re){
                Log.Error("Error=" + re.getMessage());
                continue;   // try next port if failed
            }
        }
        this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        return;
    }

    public void getHistory(String token, int roomID) {

        for (int port: GlobalConfig.ROUTE_PORTS){

            // set request
            String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
            HttpRequest request = ClientHelper.parseGetChatHistoryRequest(host, token, roomID);
            Log.Info("Send get history request to server=" + host);

            try {
                // get response
                JsonObject jsonObject = ClientHelper.getResponseValue(request);

                // validate response
                if (!isValid(jsonObject))
                    throw new RequestFailureException("Server response error" + jsonObject);

                // process valid response
                if (isSucceed(jsonObject) && jsonObject.has(GlobalConfig.HISTORY)) {
                    JsonArray jsonArray = jsonObject.get(GlobalConfig.HISTORY).getAsJsonArray();
                    ArrayList<ServerMsg> history = new ArrayList<>();
                    if (jsonArray != null) {
                        for (JsonElement e : jsonArray) {
                            ServerMsg serverMsg = new ServerMsg(e.getAsString());
                            history.add(serverMsg);
                        }
                    }
                    if (history.size() != 0)
                        history.add(new ServerMsg(GlobalConfig.CHAT, "", roomID, "===CHAT HISTORY==="));
                    this.chatHistory.put(roomID, history);
                }

                this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                return;
            } catch (RequestFailureException re){
                Log.Error("Error=" + re.getMessage());
                continue;   // try next port if failed
            }
        }
        this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        return;
    }


    public boolean isValid(JsonObject jsonObject) {
        return jsonObject != null &&
                jsonObject.has(GlobalConfig.RES_CODE) &&
                jsonObject.has(GlobalConfig.MESSAGE);
    }

    public boolean isSucceed(JsonObject jsonObject) {
        return isValid(jsonObject) &&
                jsonObject.get(GlobalConfig.RES_CODE).getAsInt() == (GlobalConfig.SUCCESS);
    }

    public String getClientMsg() {
        return clientMsg;
    }

    public void setClientMsg(String msg) {
        this.clientMsg = msg;
    }

    public List<Integer> getAvailableRoomList() {
        return availableRoomList;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public HashMap<Integer, ArrayList<ServerMsg>> getChatHistory() {
        return chatHistory;
    }

    public List<ServerMsg> getInvitedList() {
        return invitedList;
    }

    public WebSocketHandler getWebSocketHandler() {
        return webSocketHandler;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
