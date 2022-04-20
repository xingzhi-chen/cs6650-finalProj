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

public class CreateRoomHandler extends AbsRouteSvrHandler {
    private int roomServerID;

    public CreateRoomHandler(DBHelper dbHelper, Registry registry) {
        super(dbHelper, registry);
        roomServerID = 1;
    }

//    @Override
//    public void handle(HttpExchange exchange) throws IOException {
//        JSONObject body = ServerHelper.parseReqToJSON(exchange);
//        String rsp = null;
//        if (!body.has(GlobalConfig.TOKEN)) {
//            ServerHelper.writeIllegalAccessRsp(exchange);
//            return;
//        }
//
//        String username = ServerHelper.parseToken(body.getString(GlobalConfig.TOKEN));
//        int dbResCode = dbHelper.checkUsername(username);
//        if (dbResCode == ServerConfig.ERROR_NO_EXIST) {
//            ServerHelper.writeIllegalAccessRsp(exchange);
//            return;
//        } else if (dbResCode == ServerConfig.SERVER_ERROR) {
//            ServerHelper.writeServerErrorRsp(exchange);
//            return;
//        }
//
//        int trials = 0;
//        while(trials < ServerConfig.ROOM_CLUSTER_SIZE) {
//            try {
//                RoomServerInterface roomServer = (RoomServerInterface) registry.lookup(ServerConfig.RPC_ROOM_NAME + roomServerID);
//                int roomID = roomServer.createRoom(username);
//                rsp = new JSONObject()
//                        .put(GlobalConfig.RES_CODE, GlobalConfig.SUCCESS)
//                        .put(GlobalConfig.MESSAGE, GlobalConfig.errorMsg.get(GlobalConfig.SUCCESS))
//                        .put(GlobalConfig.ROOM_ID, roomID)
//                        .toString();
//                ServerHelper.writeResponse(exchange, 200, rsp);
//                return;
//            } catch (NotBoundException | RemoteException e) {
//                Log.Error("fail to connect RoomServer %d, try next server...", roomServerID);
//                e.printStackTrace();
//                roomServerID = (roomServerID + 1) % ServerConfig.ROOM_CLUSTER_SIZE;
//                trials++;
//            }
//        }
//        ServerHelper.writeIllegalAccessRsp(exchange);
//        return;
//    }

    @Override
    public void processReq(HttpExchange exchange, JSONObject body, String username) {
        int trials = 0;
        String rsp = null;
        while(trials < ServerConfig.ROOM_CLUSTER_SIZE) {
            try {
                RoomServerInterface roomServer = (RoomServerInterface) registry.lookup(ServerConfig.RPC_ROOM_NAME + roomServerID);
                int roomID = roomServer.createRoom(username);
                rsp = new JSONObject()
                        .put(GlobalConfig.RES_CODE, GlobalConfig.SUCCESS)
                        .put(GlobalConfig.MESSAGE, GlobalConfig.errorMsg.get(GlobalConfig.SUCCESS))
                        .put(GlobalConfig.ROOM_ID, roomID)
                        .toString();
                ServerHelper.writeResponse(exchange, 200, rsp);
                return;
            } catch (NotBoundException | RemoteException e) {
                Log.Error("fail to connect RoomServer %d, try next server...", roomServerID);
                e.printStackTrace();
                roomServerID = (roomServerID + 1) % ServerConfig.ROOM_CLUSTER_SIZE;
                trials++;
            }
        }
        ServerHelper.writeIllegalAccessRsp(exchange);
        return;
    }
}
