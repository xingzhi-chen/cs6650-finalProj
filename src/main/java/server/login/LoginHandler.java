package server.login;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.config.DBHelper;

import java.io.IOException;

/*
 * Handler for user login request
 */
public class LoginHandler implements HttpHandler {
    public LoginHandler(DBHelper dbHelper) {
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }
}
