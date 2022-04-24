package client.config;

import com.google.gson.Gson;
import config.GlobalConfig;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;


public class ClientHelper {


    public static JSONObject parseJSONString(String jsonString) {
        return new Gson().fromJson(jsonString, JSONObject.class);
    }

    public static HttpRequest parseRegisterRequest(String username, String password) {
        String host = String.format("http://%s:%s%s",GlobalConfig.IP_ADDRESS, GlobalConfig.LOGIN_SERVER_PORT, GlobalConfig.REGISTER_PROTOCOL);

        String requestBody = new JSONObject()
                .put(GlobalConfig.USERNAME, username)
                .put(GlobalConfig.PASSWORD, password)
                .toString();

        System.out.println("To=" + host);
        System.out.println("Request body=" + requestBody);

        return HttpRequest.newBuilder()
                .uri(URI.create(host))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }

    public static HttpRequest parseLoginRequest(String username, String password) {
        String host = String.format("http://%s:%s%s",GlobalConfig.IP_ADDRESS, GlobalConfig.LOGIN_SERVER_PORT, GlobalConfig.LOGIN_PROTOCOL);

        String requestBody = new JSONObject()
                .put(GlobalConfig.USERNAME, username)
                .put(GlobalConfig.PASSWORD, password)
                .toString();

        System.out.println("To=" + host);
        System.out.println("Request body=" + requestBody);

        return HttpRequest.newBuilder()
                .uri(URI.create(host))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }

    public static HttpRequest parseCreateRoomRequest(String token) {

        String host = String.format("http://%s:%s%s",GlobalConfig.IP_ADDRESS, GlobalConfig.ROUTE_SERVER_PORT, GlobalConfig.CREATE_ROOM_PROTOCOL);
        String requestBody = new JSONObject()
                .put(GlobalConfig.TOKEN, token)
                .toString();

        System.out.println("To=" + host);
        System.out.println("Request body=" + requestBody);

        return HttpRequest.newBuilder()
                .uri(URI.create(host))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }

    public static HttpRequest parseSendInviteRequest(String token, String otherUsername, int roomID) {

        String host = String.format("http://%s:%s%s",GlobalConfig.IP_ADDRESS, GlobalConfig.ROUTE_SERVER_PORT, GlobalConfig.INVITE_PROTOCOL);
        String requestBody = new JSONObject()
                .put(GlobalConfig.TOKEN, token)
                .put(GlobalConfig.NEW_USER, otherUsername)
                .put(GlobalConfig.ROOM_ID, roomID)
                .toString();

        System.out.println("To=" + host);
        System.out.println("Request body=" + requestBody);

        return HttpRequest.newBuilder()
                .uri(URI.create(host))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }

    public static HttpRequest parseSendMsgRequest(String token, String message, int roomID) {

        String host = String.format("http://%s:%s%s",GlobalConfig.IP_ADDRESS, GlobalConfig.ROUTE_SERVER_PORT, GlobalConfig.SEND_MSG_PROTOCOL);

        String requestBody = new JSONObject()
                .put(GlobalConfig.TOKEN, token)
                .put(GlobalConfig.MESSAGE, message)
                .put(GlobalConfig.ROOM_ID, roomID)
                .toString();

        System.out.println("To=" + host);
        System.out.println("Request body=" + requestBody);

        return HttpRequest.newBuilder()
                .uri(URI.create(host))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }
}
