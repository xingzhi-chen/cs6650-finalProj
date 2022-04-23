package server.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import config.GlobalConfig;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class ServerHelper {
    private static final long EXPIRE_DATE =
            TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS); // token expiration: 1 day
    private static final String TOKEN_SECRET = "Byv7grkmOt1oLdXOzLysoKRz7cmFEFmd0QxTAF/Calc=";

    public static String generateToken(String username) {
        Date expire = new Date(System.currentTimeMillis() + EXPIRE_DATE);
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        Map<String, Object> header = new HashMap<>(2);
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        return JWT.create()
                .withHeader(header)
                .withClaim("username", username)
                .withExpiresAt(expire)
                .sign(algorithm);
    }

    public static String parseToken(String token) throws TokenExpiredException {
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        JWTVerifier verifier = JWT.require(algorithm).build();
        // if token expired, expiration exception will be thrown
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("username").asString();
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

    public static void writeNoMatchRsp(HttpExchange exchange) {
        int resCode = GlobalConfig.NO_MATCH;
        String rsp = new JSONObject()
                .put(GlobalConfig.RES_CODE, resCode)
                .put(GlobalConfig.MESSAGE, GlobalConfig.errorMsg.get(resCode))
                .toString();
        writeResponse(exchange, 400, rsp);
    }

    public static void writeDupUsernameErrorRsp(HttpExchange exchange) {
        int resCode = GlobalConfig.DUP_USERNAME;
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
