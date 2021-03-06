package server.route;

import com.sun.net.httpserver.HttpServer;
import config.GlobalConfig;
import config.Log;
import server.config.DBHelper;
import server.config.ServerConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class RouteServer implements RouteServerInterface {
    private final int serverID;
    private WebSocketHandler webSocketHandler;
    private HttpServer httpServer;
    private final Timer timer;
    private boolean isListening = false;

    private class RaceForLeaderTask extends TimerTask {
        private final int myID;
        private final DBHelper dbHelper;
        public RaceForLeaderTask(int myID) throws NotBoundException, RemoteException {
            this.myID = myID;
            this.dbHelper = new DBHelper();
        }

        @Override
        public void run() {
            int curID = dbHelper.getMasterRouteID();
            if (curID == ServerConfig.ERROR_NO_EXIST) {
                if (listen()) {
                    dbHelper.updateMasterRouteID(myID);
                    Log.Info("set RouteServer %d to be the master server", myID);
                }
            } else if (curID == myID) {
                dbHelper.updateMasterRouteID(myID);
                if (!isListening) {
                    listen();
                }
            }
        }
    }

    public RouteServer(int serverID) throws NotBoundException, RemoteException {
        Log.Info("start Route Server %d", serverID);
        this.serverID = serverID;
        RaceForLeaderTask leaderTask = new RaceForLeaderTask(serverID);
        timer = new Timer();
        timer.scheduleAtFixedRate(leaderTask, 0, 1000 * ServerConfig.MASTER_TIMEOUT_INTERVAL / 2);
    }

    public boolean listen() {
        try {
            DBHelper dbHelper = new DBHelper();
            String host = "127.0.0.1";
            Registry registry = LocateRegistry.getRegistry(host);
            RouteServerInterface stub = (RouteServerInterface) UnicastRemoteObject.exportObject(this, 0);
            registry.rebind(ServerConfig.RPC_ROUTE_NAME, stub);

            int httpPort = GlobalConfig.ROUTE_PORTS.get(serverID - 1);
            httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
            httpServer.createContext(GlobalConfig.CREATE_ROOM_PROTOCOL, new CreateRoomHandler(dbHelper, registry));
            httpServer.createContext(GlobalConfig.SEND_MSG_PROTOCOL, new SendMsgHandler(dbHelper, registry));
            httpServer.createContext(GlobalConfig.INVITE_PROTOCOL, new InvitationHandler(dbHelper, registry));
            httpServer.createContext(GlobalConfig.INVITATION_RSP_PROTOCOL, new InvitationRspHandler(dbHelper, registry));
            httpServer.createContext(GlobalConfig.GET_HISTORY_PROTOCOL, new GetChatHistoryHandler(dbHelper, registry));
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            httpServer.setExecutor(threadPoolExecutor);
            httpServer.start();

            int websocketPort = GlobalConfig.WEBSOCKET_PORTS.get(serverID - 1);
            webSocketHandler = new WebSocketHandler(dbHelper, websocketPort);
            webSocketHandler.start();
            isListening = true;
            return true;
        } catch (NotBoundException | IOException e) {
            e.printStackTrace();
            isListening = false;
            return false;
        }
    }

    @Override
    public void sendMsgToClient(String toUser, String msg) throws RemoteException {
        try {
            webSocketHandler.sendMsgToClient(toUser, msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws NotBoundException, RemoteException {
        if (args.length < 1 || args[0].length() == 0) {
            Log.Error("missing RouteServer ID, process stop....");
            System.exit(1);
        }
        RouteServer routeServer = new RouteServer(Integer.parseInt(args[0]));
    }
}
