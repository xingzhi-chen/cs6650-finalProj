package server.login;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.GlobalConfig;
import org.json.JSONObject;
import server.config.DBHelper;
import server.config.ServerConfig;
import server.config.ServerHelper;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import java.io.IOException;
import java.util.ArrayList;

/*
 * Handler for user login request
 */
public class LoginHandler implements HttpHandler {
    public static DBHelper dbHelper;

    public LoginHandler() throws NotBoundException, RemoteException {
        dbHelper = new DBHelper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject body = ServerHelper.parseReqToJSON(exchange);
        // validate request
        if (!body.has(GlobalConfig.USERNAME) || !body.has(GlobalConfig.PASSWORD)) {
            ServerHelper.writeWrongReqRsp(exchange);
            return;
        }
        String username = body.getString(GlobalConfig.USERNAME);
        String password = body.getString(GlobalConfig.PASSWORD);

        if (ifUsernameExist(username)) {
            // if user exists in db, match password
            boolean loginSuccess = dbHelper.matchUsernamePassword(username, password);
            if (loginSuccess) {
                // issue token
                String token = ServerHelper.generateToken(username);
                ArrayList<String> userRoomList = dbHelper.getUserRoomList(username);
                // return token and roomList in response
                String rsp = new JSONObject()
                        .put(GlobalConfig.RES_CODE, GlobalConfig.SUCCESS)
                        .put(GlobalConfig.MESSAGE, GlobalConfig.errorMsg.get(GlobalConfig.SUCCESS))
                        .put(GlobalConfig.TOKEN, token)
                        .put(GlobalConfig.ROOM_LIST, userRoomList)
                        .toString();
                ServerHelper.writeResponse(exchange, 200, rsp);
            } else {
                ServerHelper.writeNoMatchRsp(exchange);
            }
        } else {
            ServerHelper.writeNoMatchRsp(exchange);
        }
    }

    private boolean ifUsernameExist(String username) {
        return dbHelper.checkUsername(username) == ServerConfig.SUCCESS;
    }
}

