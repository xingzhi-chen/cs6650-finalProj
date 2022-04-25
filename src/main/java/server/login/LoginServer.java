package server.login;

import com.sun.net.httpserver.HttpServer;
import config.GlobalConfig;
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
//        if (args.length < 1 || args[0].length() == 0) {
//            Log.Error("missing Login ID, process stop....");
//            System.exit(1);
 //       }
        int port = 8090;
        try {
            LoginServer loginServer = new LoginServer();

//            int port = GlobalConfig.LOGIN_PORTS.get(Integer.parseInt(args[0]) - 1);
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/login", new LoginHandler());
            server.createContext("/register", new RegisterHandler());

            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            server.setExecutor(threadPoolExecutor);
            server.start();
            Log.Info("Login server %d started on port %d", Integer.parseInt(args[0]), port);
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
