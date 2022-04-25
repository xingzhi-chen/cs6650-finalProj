package server.route;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.GlobalConfig;
import config.Log;
import org.json.JSONObject;
import server.config.DBHelper;
import server.config.ServerConfig;
import server.config.ServerHelper;
import java.io.IOException;
import java.rmi.registry.Registry;

/*
* abstract class for Http handlers of RouteServer, always check the token in the request first
 */
public abstract class AbsRouteSvrHandler implements HttpHandler {
    protected final DBHelper dbHelper;
    protected final Registry registry;

    public AbsRouteSvrHandler(DBHelper dbHelper, Registry registry) {
        this.dbHelper = dbHelper;
        this.registry = registry;
    }

    @Override
    public void handle(HttpExchange exchange) {
        JSONObject body = ServerHelper.parseReqToJSON(exchange);
        String rsp = null;
        if (!body.has(GlobalConfig.TOKEN)) {
            ServerHelper.writeWrongReqRsp(exchange);
            return;
        }

        try {
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
        } catch (TokenExpiredException exp) {
            Log.Warn("receive expired token: %s", body.getString(GlobalConfig.TOKEN));
            ServerHelper.writeTokenExpRsp(exchange);
        }
    }

    // function for actual handler logic, to be overwritten
    public void processReq(HttpExchange exchange, JSONObject body, String username) {
        return;
    }
}
