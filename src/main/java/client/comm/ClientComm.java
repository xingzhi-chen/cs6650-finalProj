package client.comm;


import client.config.ClientHelper;
import client.config.RequestFailureException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import config.GlobalConfig;
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

    private String token;
    private String username;

    private List<ServerMsg> invitedList;
    private List<Integer> availableRoomList;
    private WebSocketHandler webSocketHandler;

    private String clientMsg;
    private HashMap<Integer, ArrayList<ServerMsg>> chatHistory;


    public ClientComm() {
    }

    public static void main(String[] args) {
        ClientComm comm = new ClientComm();

        System.out.println("\n==Test register==");
        comm.register("user", "password");

        System.out.println("\n==Test login==");
        comm.login("user", "password");

        System.out.println("\n==Test ws");
        comm.websocketConnection(comm.getToken());

        System.out.println("\n==Test createRoom");
        comm.createRoom(comm.token);
//
//        System.out.println("\n==Test inv");
//        comm.sendInvitation("token", "user2", 9732);

        System.out.println("\n==Test send msg");
        comm.sendMessage(comm.getToken(), "XXXXXXXXX", 3443);

        System.out.println("====");
        System.out.println(comm.chatHistory);

    }

    @Override
    public void register(String username, String password) {
        for (int port: GlobalConfig.LOGIN_PORTS){
                String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
                HttpRequest request = ClientHelper.parseRegisterRequest(host, username, password);
                System.out.println("Request for sign up to server=" + host);

                try {
                    // get response body
                    JsonObject jsonObject = getResponseValue(request);

                    // validate response format
                    if (!ClientHelper.isValidResponse(GlobalConfig.REGISTER_PROTOCOL, jsonObject))
                        throw new RequestFailureException("Server response error" + jsonObject);

                    // valid response
                    this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                    if (isSucceed(jsonObject)) {}
                    return;

                } catch (RequestFailureException re){
                    System.out.println("Error=" + re.getMessage());
                    continue;   // try next port if failed
                }
        }
        this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        return;
    }

    @Override
    public void login(String username, String password) {
        for (int port: GlobalConfig.LOGIN_PORTS){
            String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
            HttpRequest request = ClientHelper.parseLoginRequest(host, username, password);
            System.out.println("Request for login in to server=" + host);
            try {
                // get response body
                JsonObject jsonObject = getResponseValue(request);

                // validate response format
                if (!ClientHelper.isValidResponse(GlobalConfig.LOGIN_PROTOCOL, jsonObject))
                    throw new RequestFailureException("Server response error" + jsonObject);

                // valid response
                this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();

                if (isSucceed(jsonObject)) {
                    // store username
                    this.username = username;
                    // get token
                    this.token = jsonObject.get(GlobalConfig.TOKEN).getAsString();
                    // connect to websocket
                    websocketConnection(this.token);

                    // get room list and chat history
                    this.availableRoomList = new ArrayList<>();
                    this.chatHistory = new HashMap<>();
                    JsonArray jsonArray = jsonObject.get(GlobalConfig.ROOM_LIST).getAsJsonArray();
                    if (jsonArray != null) {
                        for (JsonElement element : jsonArray) {
                            int roomID = element.getAsInt();
                            this.availableRoomList.add(roomID);
                            getHistory(this.token, roomID);
                        }
                    }
                }
                return;

            } catch (RequestFailureException re){
                System.out.println("Error=" + re.getMessage());
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

        for (int port: GlobalConfig.WEBSOCKET_PORTS) { // Try different websocket ports
            String host = String.format("ws://%s:%s", GlobalConfig.IP_ADDRESS, port);
            webSocketHandler = new WebSocketHandler(URI.create(host)) {
                @Override
                public void onMessage(String s) {
                    System.out.println("msg=" + s);
                    ServerMsg serverMsg = new ServerMsg(s);
                    if (serverMsg.getMsgType() == GlobalConfig.SYSTEM) {
                        clientMsg = GlobalConfig.errorMsg.get(Integer.valueOf(serverMsg.getMsg()));
                        if (Integer.valueOf(serverMsg.getMsg()).equals(GlobalConfig.SUCCESS))
                            this.connected = true;
                    } else if (serverMsg.getMsgType() == GlobalConfig.CHAT) {
                        chatHistory.get(serverMsg.getRoomId()).add(serverMsg);
                    } else if (serverMsg.getMsgType() == GlobalConfig.INVITATION) {
                        invitedList.add(serverMsg);
                    } else {
                        clientMsg = "Cannot resolve websocket message from server.";
                    }
                }
            };

            try {
                webSocketHandler.connectBlocking(GlobalConfig.SERVER_TIMEOUT, MILLISECONDS);
                System.out.println("Verify token..." + token);
                webSocketHandler.send(
                        new JSONObject()
                                .put(GlobalConfig.TOKEN, token)
                                .toString());
                Thread.sleep(500); // TODO wait notify
                if (this.webSocketHandler.connected) {
                    this.clientMsg = "Welcome to the chat room";
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
                continue;
            }
        }
    }

    @Override
    public void createRoom(String token) {
        for (int port: GlobalConfig.ROUTE_PORTS){
            String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
            HttpRequest request = ClientHelper.parseCreateRoomRequest(host, token);
            System.out.println("Request for create room to server=" + host);
            try {
                // get response body
                JsonObject jsonObject = getResponseValue(request);

                // validate response format
                if (!ClientHelper.isValidResponse(GlobalConfig.CREATE_ROOM_PROTOCOL, jsonObject))
                    throw new RequestFailureException("Server response error" + jsonObject);

                // valid response
                this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();

                if (isSucceed(jsonObject)) {
                    int roomID = jsonObject.get(GlobalConfig.ROOM_ID).getAsInt();
                    this.availableRoomList.add(roomID);
                    this.chatHistory.put(roomID, new ArrayList<>());
                }
                return;

            } catch (RequestFailureException re){
                System.out.println("Error=" + re.getMessage());
                continue;   // try next port if failed
            }
        }
        this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        return;
    }

    @Override
    public void sendInvitation(String token, String otherUsername, int roomID) {
        //HttpRequest request = ClientHelper.parseSendInviteRequest(host, token, otherUsername, roomID);
        //sendRequest(request);
    }

    @Override
    public void sendMessage(String token, String message, int roomID) {
        for (int port: GlobalConfig.ROUTE_PORTS){
            String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
            HttpRequest request = ClientHelper.parseSendMsgRequest(host, token, message, roomID);
            System.out.println("Request for sending message to server=" + host);

            try {
                // get response body
                JsonObject jsonObject = getResponseValue(request);

                // validate response format
                if (!ClientHelper.isValidResponse(GlobalConfig.SEND_MSG_PROTOCOL, jsonObject))
                    throw new RequestFailureException("Server response error" + jsonObject);

                // valid response
                this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                if (isSucceed(jsonObject)) {}
                return;

            } catch (RequestFailureException re){
                System.out.println("Error=" + re.getMessage());
                continue;   // try next port if failed
            }
        }
        this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        return;
    }

    public void getHistory(String token, int roomID) {
        for (int port: GlobalConfig.ROUTE_PORTS){
            String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
            HttpRequest request = ClientHelper.parseGetChatHistoryRequest(host, token, roomID);
            System.out.println("Request for getting chat history to server=" + host);

            try {
                // get response body
                JsonObject jsonObject = getResponseValue(request);

                // validate response format
                if (!ClientHelper.isValidResponse(GlobalConfig.GET_HISTORY_PROTOCOL, jsonObject))
                    throw new RequestFailureException("Server response error" + jsonObject);

                // valid response
                this.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                if (isSucceed(jsonObject)) {

                    JsonArray jsonArray = jsonObject.get(GlobalConfig.HISTORY).getAsJsonArray();
                    ArrayList<ServerMsg> history = new ArrayList<>();
                    if (jsonArray != null) {
                        for (JsonElement e : jsonArray) {
                            ServerMsg serverMsg = new ServerMsg(e.getAsString());
                            history.add(serverMsg);
                        }
                    }
                    history.add(new ServerMsg(GlobalConfig.CHAT, "", roomID, "============"));
                    this.chatHistory.put(roomID, history);
                }
                return;
            } catch (RequestFailureException re){
                System.out.println("Error=" + re.getMessage());
                continue;   // try next port if failed
            }
        }
        this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        return;
    }

    private JsonObject getResponseValue(HttpRequest request) throws RequestFailureException{
        try {
            HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response=" + response.body());
            return (JsonObject) new Gson().fromJson(response.body(), JsonElement.class);
        } catch (HttpTimeoutException e) {
            e.printStackTrace();
            throw new RequestFailureException("Http server time out.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RequestFailureException(e.getMessage());
        }
    }

    public boolean isSucceed(JsonObject jsonObject) {
        return jsonObject.has(GlobalConfig.RES_CODE) &&
                jsonObject.has(GlobalConfig.MESSAGE) &&
                jsonObject.get(GlobalConfig.RES_CODE).getAsInt() == (GlobalConfig.SUCCESS);
    }

    public String getClientMsg() {
        return clientMsg;
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
}
