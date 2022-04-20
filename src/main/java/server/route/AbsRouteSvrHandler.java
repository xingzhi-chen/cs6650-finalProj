package server.route;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.GlobalConfig;
import config.Log;
import org.json.JSONObject;
import server.config.DBHelper;
import server.config.ServerConfig;
import server.config.ServerHelper;
import server.room.RoomServerInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public abstract class AbsRouteSvrHandler implements HttpHandler {
    protected final DBHelper dbHelper;
    protected final Registry registry;

    public AbsRouteSvrHandler(DBHelper dbHelper, Registry registry) {
        this.dbHelper = dbHelper;
        this.registry = registry;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject body = ServerHelper.parseReqToJSON(exchange);
        String rsp = null;
        if (!body.has(GlobalConfig.TOKEN)) {
            ServerHelper.writeWrongReqRsp(exchange);
            return;
        }

        String username = ServerHelper.parseToken(body.getString(GlobalConfig.TOKEN));
        int dbResCode = dbHelper.checkUsername(username);
        if (dbResCode == ServerConfig.ERROR_NO_EXIST) {
            ServerHelper.writeIllegalAccessRsp(exchange);
            return;
        } else if (dbResCode == ServerConfig.SERVER_ERROR) {
            ServerHelper.writeServerErrorRsp(exchange);
            return;
        }

        processReq(exchange, body, username);
    }

    public void processReq(HttpExchange exchange, JSONObject body, String username) {
        return;
    }
}
