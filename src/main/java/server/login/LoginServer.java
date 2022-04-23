package server.login;

import com.sun.net.httpserver.HttpServer;
import config.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class LoginServer implements LoginServerInterface{
    public LoginServer() throws NotBoundException, RemoteException {
    }

    public static void main(String[] args) {
        try {
            LoginServer loginServer = new LoginServer();

            HttpServer server = HttpServer.create(new InetSocketAddress(8090), 0);
            server.createContext("/login", new LoginHandler());
            server.createContext("/register", new RegisterHandler());

            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            server.setExecutor(threadPoolExecutor);
            server.start();
            Log.Info("Login server started on port 8090");
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
