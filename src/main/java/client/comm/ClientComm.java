package client.comm;


import client.config.ClientHelper;
import config.GlobalConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    private List<Integer> createdIDList;
    private List<Integer> invitedIDList;
    private List<Integer> availableIDList;
    private WebSocketHandler webSocketHandler;
    private String msg = "empty msg";


    public ClientComm() {
        this.createdIDList = new ArrayList<>();
        this.invitedIDList = new ArrayList<>();
        this.availableIDList = new ArrayList<>();
    }

    public static void main(String[] args) {
        ClientComm comm = new ClientComm();

        System.out.println("\n==Test register==");
        comm.register("user", "password");

        System.out.println("\n==Test login==");
        comm.login("user2", "password2");

        System.out.println("\n==Test ws");
        comm.websocketConnection("token");

        System.out.println("\n==Test createRoom");
        comm.createRoom("token");

        System.out.println("\n==Test ws");
        comm.sendInvitation("token", "user2", 0);

        System.out.println("\n==Test send msg");
        comm.sendMessage("token", "I am user", 0);

    }

    @Override
    public void register(String username, String password) {
        HttpRequest request = ClientHelper.parseRegisterRequest(username, password);
        sendRequest(request);
    }

    @Override
    public void login(String username, String password) {
        HttpRequest request = ClientHelper.parseLoginRequest(username, password);
        sendRequest(request);
    }

    @Override
    public void websocketConnection(String token) {
        URI routeWebsocketAddress = URI.create(
                String.format("ws://%s:%s",
                GlobalConfig.IP_ADDRESS,
                GlobalConfig.ROUTE_SERVER_PORT + 1));
        WebSocketHandler webSocketHandler = new WebSocketHandler(routeWebsocketAddress);
        try {
            webSocketHandler.connectBlocking(GlobalConfig.SERVER_TIMEOUT, MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Verify token...");
        JSONObject verifyToken = new JSONObject().put(GlobalConfig.TOKEN, token);
        webSocketHandler.send(verifyToken.toString());
    }

    @Override
    public void createRoom(String token) {
        HttpRequest request = ClientHelper.parseCreateRoomRequest(token);
        sendRequest(request);
    }

    @Override
    public void sendInvitation(String token, String otherUsername, int roomID) {
        HttpRequest request = ClientHelper.parseSendInviteRequest(token, otherUsername, roomID);
        sendRequest(request);
    }

    @Override
    public void sendMessage(String token, String message, int roomID) {
        HttpRequest request = ClientHelper.parseSendMsgRequest(token, message, roomID);
        sendRequest(request);
    }

    private void sendRequest(HttpRequest request) {
        try {
            HttpResponse<String> response =
                    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            this.msg = response.body();
            System.out.println("Response=" + response.body());

            // check status code
            if (response == null) {
                System.out.println("No response from Server.");
                return;
            }
            if (response.statusCode() != 200) {
                System.out.println("Bad Status: " + response.statusCode());
                return;
            }

            // check resCode and print message
            JSONObject jsonObject = ClientHelper.parseJSONString(response.body());
            if (!jsonObject.has(GlobalConfig.RES_CODE) || !jsonObject.has(GlobalConfig.MESSAGE)){
                System.out.println("Unrecognized response. " + response.body());
            }

            if (jsonObject.get(GlobalConfig.RES_CODE).equals(GlobalConfig.SUCCESS)){
                System.out.println("Message sent.");
            }
            System.out.println(jsonObject.get(GlobalConfig.MESSAGE));

        } catch (HttpTimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getMsg(){
        return this.msg;
    }
}
