package server.route;

import com.sun.net.httpserver.HttpServer;
import server.config.DBHelper;
import server.config.ServerConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class RouteServer implements RouteServerInterface {
    private final WebSocketHandler webSocketHandler;
    private final HttpServer httpServer;

    public RouteServer(int port) throws NotBoundException, IOException {
        DBHelper dbHelper = new DBHelper();
        String host = "127.0.0.1";
        Registry registry = LocateRegistry.getRegistry(host);
        RouteServerInterface stub = (RouteServerInterface) UnicastRemoteObject.exportObject(this, 0);
        registry.rebind(ServerConfig.RPC_ROUTE_NAME, stub);

        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/create_room", new CreateRoomHandler(dbHelper, registry));
        httpServer.createContext("/send_msg", new SendMsgHandler(dbHelper, registry));
        httpServer.createContext("/invite", new InvitationHandler(dbHelper, registry));
        httpServer.createContext("/invitation_rsp", new InvitationRspHandler(dbHelper, registry));
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        httpServer.setExecutor(threadPoolExecutor);
        httpServer.start();

        webSocketHandler = new WebSocketHandler(dbHelper, port+1);
        webSocketHandler.start();
    }

    @Override
    public void sendMsgToClient(String toUser, String msg) throws RemoteException {
        try {
            webSocketHandler.sendMsgToClient(toUser, msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            RouteServer routeServer = new RouteServer(8080);
        } catch (NotBoundException | IOException e) {
            e.printStackTrace();
        }
    }
}
