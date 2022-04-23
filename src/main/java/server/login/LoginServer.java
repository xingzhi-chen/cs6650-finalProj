package server.login;

import com.sun.net.httpserver.HttpServer;
import server.config.DBHelper;
import server.config.ServerConfig;
import server.route.RouteServerInterface;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class LoginServer {
    private static DBHelper dbHelper;
    private static final String host = "127.0.0.1";

    public LoginServer() throws NotBoundException, RemoteException {
        dbHelper = new DBHelper();
    }
    private boolean ifUsernameExist(String username) {
        return true;
    }

    private boolean checkLoginMatch(String username, String password) {
        return true;
    }

    public static void main(String[] args) {
        try {
            LoginServer loginServer = new LoginServer();

            HttpServer server = HttpServer.create(new InetSocketAddress(8090), 0);
            server.createContext("/login", new LoginHandler(dbHelper));
            server.createContext("/register", new RegisterHandler(dbHelper));

            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            server.setExecutor(threadPoolExecutor);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
