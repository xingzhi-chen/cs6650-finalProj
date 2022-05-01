package client.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import config.GlobalConfig;
import config.Log;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;


public class ClientHelper {

    public static final String GENERAL_ERROR_MSG = "Cannot resolve the request. Please try again later.";

    public static HttpRequest parseRegisterRequest(String host, String username, String password) {
        String requestBody = new JSONObject()
                .put(GlobalConfig.USERNAME, username)
                .put(GlobalConfig.PASSWORD, password)
                .toString();

        return HttpRequest.newBuilder()
                .uri(URI.create(host + GlobalConfig.REGISTER_PROTOCOL))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }

    public static HttpRequest parseLoginRequest(String host, String username, String password) {
        String requestBody = new JSONObject()
                .put(GlobalConfig.USERNAME, username)
                .put(GlobalConfig.PASSWORD, password)
                .toString();

        return HttpRequest.newBuilder()
                .uri(URI.create(host + GlobalConfig.LOGIN_PROTOCOL))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }

    public static HttpRequest parseCreateRoomRequest(String host, String token) {
        String requestBody = new JSONObject()
                .put(GlobalConfig.TOKEN, token)
                .toString();

        return HttpRequest.newBuilder()
                .uri(URI.create(host + GlobalConfig.CREATE_ROOM_PROTOCOL))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }

    public static HttpRequest parseSendInviteRequest(String host, String token, String otherUsername, int roomID) {
        String requestBody = new JSONObject()
                .put(GlobalConfig.TOKEN, token)
                .put(GlobalConfig.NEW_USER, otherUsername)
                .put(GlobalConfig.ROOM_ID, roomID)
                .toString();

        return HttpRequest.newBuilder()
                .uri(URI.create(host + GlobalConfig.INVITE_PROTOCOL))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }

    public static HttpRequest parseSendInviteRspRequest(String host, String token, int roomID, boolean accept) {
        String requestBody = new JSONObject()
                .put(GlobalConfig.TOKEN, token)
                .put(GlobalConfig.ROOM_ID, roomID)
                .put(GlobalConfig.ACCEPT, accept)
                .toString();

        return HttpRequest.newBuilder()
                .uri(URI.create(host + GlobalConfig.INVITATION_RSP_PROTOCOL))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }


    public static HttpRequest parseSendMsgRequest(String host, String token, String message, int roomID) {

        String requestBody = new JSONObject()
                .put(GlobalConfig.TOKEN, token)
                .put(GlobalConfig.MESSAGE, message)
                .put(GlobalConfig.ROOM_ID, roomID)
                .toString();

        return HttpRequest.newBuilder()
                .uri(URI.create(host + GlobalConfig.SEND_MSG_PROTOCOL))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }

    public static HttpRequest parseGetChatHistoryRequest(String host, String token, int roomID) {

        String requestBody = new JSONObject()
                .put(GlobalConfig.TOKEN, token)
                .put(GlobalConfig.ROOM_ID, roomID)
                .toString();

        return HttpRequest.newBuilder()
                .uri(URI.create(host + GlobalConfig.GET_HISTORY_PROTOCOL))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(GlobalConfig.SERVER_TIMEOUT))
                .build();
    }

    public static JsonObject getResponseValue(HttpRequest request) throws RequestFailureException{
        try {
            HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Log.Info("Response=" + response.body());
            return (JsonObject) new Gson().fromJson(response.body(), JsonElement.class);
        } catch (HttpTimeoutException e) {
            //e.printStackTrace();
            throw new RequestFailureException("Http server time out.");
        } catch (Exception e) {
            //e.printStackTrace();
            throw new RequestFailureException(e.getMessage());
        }
    }
}
