package server.config;

import com.sun.net.httpserver.HttpExchange;
import config.GlobalConfig;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ServerHelper {
    public static String generateToken(String username) {
        return "testToken";
    }

    public static String parseToken(String token) {
        return "testUsername";
    }

    public static JSONObject parseReqToJSON(HttpExchange exchange) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String bodyStr = reader.lines().collect(Collectors.joining());
        return new JSONObject(bodyStr);
    }

    public static void writeResponse(HttpExchange exchange, int statusCode, String rsp) {
        try {
            exchange.sendResponseHeaders(statusCode, rsp.length());
            OutputStream os = exchange.getResponseBody();
            os.write(rsp.getBytes(StandardCharsets.UTF_8));
            os.close();
            exchange.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeDefaultSuccessRsp(HttpExchange exchange) {
        int resCode = GlobalConfig.SUCCESS;
        String rsp = new JSONObject()
                .put(GlobalConfig.RES_CODE, resCode)
                .put(GlobalConfig.MESSAGE, GlobalConfig.errorMsg.get(resCode))
                .toString();
        writeResponse(exchange, 200, rsp);
    }

    public static void writeIllegalAccessRsp(HttpExchange exchange) {
        int resCode = GlobalConfig.NO_MATCH;
        String rsp = new JSONObject()
                .put(GlobalConfig.RES_CODE, resCode)
                .put(GlobalConfig.MESSAGE, GlobalConfig.errorMsg.get(resCode))
                .toString();
        writeResponse(exchange, 401, rsp);
    }

    public static void writeWrongReqRsp(HttpExchange exchange) {
        int resCode = GlobalConfig.MISSING_ARGS;
        String rsp = new JSONObject()
                .put(GlobalConfig.RES_CODE, resCode)
                .put(GlobalConfig.MESSAGE, GlobalConfig.errorMsg.get(resCode))
                .toString();
        writeResponse(exchange, 400, rsp);
    }

    public static void writeNoRoomRsp(HttpExchange exchange) {
        int resCode = GlobalConfig.NO_ROOM;
        String rsp = new JSONObject()
                .put(GlobalConfig.RES_CODE, resCode)
                .put(GlobalConfig.MESSAGE, GlobalConfig.errorMsg.get(resCode))
                .toString();
        writeResponse(exchange, 400, rsp);
    }

    public static void writeServerErrorRsp(HttpExchange exchange) {
        int resCode = GlobalConfig.SERVER_ERROR;
        String rsp = new JSONObject()
                .put(GlobalConfig.RES_CODE, resCode)
                .put(GlobalConfig.MESSAGE, GlobalConfig.errorMsg.get(resCode))
                .toString();
        writeResponse(exchange, 500, rsp);
    }
}
