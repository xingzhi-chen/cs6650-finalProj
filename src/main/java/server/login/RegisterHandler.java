package server.login;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.GlobalConfig;
import org.json.JSONObject;
import server.config.DBHelper;
import server.config.ServerConfig;
import server.config.ServerHelper;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/*
 * Handler for user register request
 */
public class RegisterHandler implements HttpHandler {
    public static DBHelper dbHelper;

    public RegisterHandler() throws NotBoundException, RemoteException {
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
        String username= body.getString(GlobalConfig.USERNAME);
        String password = body.getString(GlobalConfig.PASSWORD);
        // check if the user is new to db
        if (!ifUsernameExist(username)){
            if (dbHelper.addNewUser(username, password)==ServerConfig.SUCCESS
            && dbHelper.initUserRoomList(username)==ServerConfig.SUCCESS){
                String rsp = new JSONObject()
                        .put(GlobalConfig.RES_CODE, GlobalConfig.SUCCESS)
                        .put(GlobalConfig.MESSAGE, GlobalConfig.errorMsg.get(GlobalConfig.SUCCESS))
                        .put(GlobalConfig.USERNAME, username)
                        .toString();
                ServerHelper.writeResponse(exchange, 200, rsp);
            } else {
                ServerHelper.writeServerErrorRsp(exchange);
            }
        } else {
            // if username exists, return Dup Username error
            ServerHelper.writeDupUsernameErrorRsp(exchange);
        }
    }

    private boolean ifUsernameExist(String username) {
        return dbHelper.checkUsername(username) == ServerConfig.SUCCESS;
    }
}
