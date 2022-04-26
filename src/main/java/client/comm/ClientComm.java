package client.comm;


import client.config.ClientHelper;
import client.config.RequestFailureException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import config.GlobalConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ClientComm implements ClientCommInterface{

    private String token;
    private int currentRoomID;
    private List<Integer> invitedIDList;
    private List<Integer> availableRoomList;
    private WebSocketHandler webSocketHandler;

    private boolean succeed;
    private JsonObject jsonObject;
    private String clientMsg;


    public ClientComm() {
    }

    public static void main(String[] args) {
        ClientComm comm = new ClientComm();

        System.out.println("\n==Test register==");
        comm.register("user2", "password2");

        System.out.println("\n==Test login==");
        comm.login("user2", "password");

        System.out.println("\n==Test ws");
        comm.websocketConnection(comm.token);
//
//        System.out.println("\n==Test createRoom");
//        comm.createRoom("token");
//
//        System.out.println("\n==Test ws");
//        comm.sendInvitation("token", "user2", 0);
//
//        System.out.println("\n==Test send msg");
//        comm.sendMessage("token", "I am user", 0);

    }

    @Override
    public void register(String username, String password) {
        for (int port: GlobalConfig.LOGIN_PORTS){
                String host = String.format("http://%s:%s", GlobalConfig.IP_ADDRESS, port);
                HttpRequest request = ClientHelper.parseRegisterRequest(host, username, password);
                System.out.println("Request for register to server=" + host);

                try {
                    // get response body
                    this.jsonObject = getResponseValue(request);

                    // validate response format
                    if (!ClientHelper.isValidResponse(GlobalConfig.REGISTER_PROTOCOL, this.jsonObject))
                        throw new RequestFailureException("Server response error" + this.jsonObject);

                    // valid response
                    this.clientMsg = this.jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                    if (isSucceed(this.jsonObject)) {}
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
            System.out.println("Send Request to server=" + host);
            try {
                // get response body
                this.jsonObject = getResponseValue(request);

                // validate response format
                if (!ClientHelper.isValidResponse(GlobalConfig.LOGIN_PROTOCOL, this.jsonObject))
                    throw new RequestFailureException("Server response error" + this.jsonObject);

                // valid response
                this.clientMsg = this.jsonObject.get(GlobalConfig.MESSAGE).getAsString();
                System.out.println("is?" + isSucceed(this.jsonObject));
                if (isSucceed(this.jsonObject)) {
                    // get token
                    this.token = this.jsonObject.get(GlobalConfig.TOKEN).getAsString();
                    // connect to websocket
                    websocketConnection(this.token);

                    // get room list
                    this.availableRoomList = new ArrayList<>();
                    JsonArray jsonArray = this.jsonObject.get(GlobalConfig.ROOM_LIST).getAsJsonArray();
                    if (jsonArray != null) {
                        for (JsonElement element : jsonArray) {
                            this.availableRoomList.add(element.getAsInt());
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
            this.succeed = false;
            this.clientMsg = "Cannot connect to server.";
            return;
        }

        // TODO 2 server

        this.succeed = false;
        String host = String.format(
                "ws://%s:%s",
                GlobalConfig.IP_ADDRESS,
                GlobalConfig.ROUTE_SERVER_PORT + 1);
        URI routeWebsocketAddress = URI.create(host);

        System.out.println("there?" + host);
        try {
            WebSocketHandler webSocketHandler = new WebSocketHandler(routeWebsocketAddress);
            webSocketHandler.connectBlocking(GlobalConfig.SERVER_TIMEOUT, MILLISECONDS);
            System.out.println("Verify token..." + token);
            JsonObject verifyToken = new JsonObject();
            verifyToken.addProperty(GlobalConfig.TOKEN, token);
            webSocketHandler.send(verifyToken.toString());

            // TODO no response for n seconds

            Thread.sleep(GlobalConfig.SERVER_TIMEOUT);
            if (webSocketHandler.connectionComplete) {
                this.clientMsg = "Welcome to the chat room system";
                this.succeed = true;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            this.clientMsg = ClientHelper.GENERAL_ERROR_MSG;
        }



    }

    @Override
    public void createRoom(String token) {
        HttpRequest request = ClientHelper.parseCreateRoomRequest(token);
        //sendRequest(request);
    }

    @Override
    public void sendInvitation(String token, String otherUsername, int roomID) {
        HttpRequest request = ClientHelper.parseSendInviteRequest(token, otherUsername, roomID);
        //sendRequest(request);
    }

    @Override
    public void sendMessage(String token, String message, int roomID) {
        HttpRequest request = ClientHelper.parseSendMsgRequest(token, message, roomID);
        //sendRequest(request);
    }

    private JsonObject getResponseValue(HttpRequest request) throws RequestFailureException{
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response=" + response.body());
            return new Gson().fromJson(response.body(), JsonObject.class);

        } catch (HttpTimeoutException e) {
            e.printStackTrace();
            throw new RequestFailureException("Http server time out.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RequestFailureException(e.getMessage());
        }
    }

    public boolean isSucceed(JsonObject jsonObject) {
        this.succeed =
                jsonObject.has(GlobalConfig.RES_CODE) &&
                jsonObject.has(GlobalConfig.MESSAGE) &&
                jsonObject.get(GlobalConfig.RES_CODE).getAsInt() == (GlobalConfig.SUCCESS);
        return this.succeed;
    }

    public boolean isSucceed() {
        return succeed;
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
}
