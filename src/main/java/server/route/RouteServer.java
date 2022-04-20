package server.route;

import com.sun.net.httpserver.HttpServer;
import server.config.DBHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class RouteServer {
    public static void main(String[] args) {
        try {
            DBHelper dbHelper = new DBHelper();
            String host = "127.0.0.1";
            Registry registry = LocateRegistry.getRegistry(host);

            HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
            server.createContext("/create_room", new CreateRoomHandler(dbHelper, registry));
            server.createContext("/send_msg", new SendMsgHandler(dbHelper, registry));

            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            server.setExecutor(threadPoolExecutor);
            server.start();
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
