package client.comm;

import client.config.ClientHelper;
import client.config.RequestFailureException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import config.GlobalConfig;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;

public class HttpHandler {

    ClientComm comm;

    public HttpHandler(ClientComm comm) {
        this.comm = comm;
    }

    public boolean isValidResponse(JsonObject jsonObject) {
        return jsonObject.has(GlobalConfig.RES_CODE)
                && jsonObject.has(GlobalConfig.MESSAGE);
    }

    public boolean isSucceed(JsonObject jsonObject) {
        return isValidResponse(jsonObject) &&
                jsonObject.get(GlobalConfig.RES_CODE).getAsInt() == (GlobalConfig.SUCCESS);
    }

    public JsonObject getResponseValue(HttpRequest request) throws RequestFailureException {
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

    public void processRsp (HttpRequest request, String protocol) throws RequestFailureException {
        // get response body
        JsonObject jsonObject = getResponseValue(request);

        // validate response format
        if (!ClientHelper.isValidResponse(protocol, jsonObject))
            throw new RequestFailureException("Server response error" + jsonObject);

        // valid response
        if (isSucceed(jsonObject)) {
            processSuccessRsp(jsonObject);
        }
        this.comm.clientMsg = jsonObject.get(GlobalConfig.MESSAGE).getAsString();
    }

    public void processSuccessRsp (JsonObject jsonObject){

    }
}
