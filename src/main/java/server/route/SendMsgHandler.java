package server.route;

import com.sun.net.httpserver.HttpExchange;
import config.GlobalConfig;
import org.json.JSONObject;
import server.config.DBHelper;
import server.config.DBRsp;
import server.config.ServerConfig;
import server.config.ServerHelper;
import server.room.RoomServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.HashMap;

public class SendMsgHandler extends AbsRouteSvrHandler {
    private final HashMap<Integer, String> roomIDToAddr;

    public SendMsgHandler(DBHelper dbHelper, Registry registry) {
        super(dbHelper, registry);
        this.roomIDToAddr = new HashMap<>();
    }

    @Override
    public synchronized void processReq(HttpExchange exchange, JSONObject body, String username) {
        if (!body.has(GlobalConfig.ROOM_ID) || !body.has(GlobalConfig.MESSAGE)) {
            ServerHelper.writeWrongReqRsp(exchange);
            return;
        }

        int roomID = body.getInt(GlobalConfig.ROOM_ID);
        String msg = body.getString(GlobalConfig.MESSAGE);
        // if no address in cache, update cache
        if (!roomIDToAddr.containsKey(roomID)) {
            DBRsp dbRsp = dbHelper.getRoomAddress(roomID);
            if (dbRsp.getResCode() == ServerConfig.ERROR_NO_EXIST) {
                ServerHelper.writeNoRoomRsp(exchange);
                return;
            }
            roomIDToAddr.put(roomID, dbRsp.getValue().get(0));
        }
        String addr = roomIDToAddr.get(roomID);

        try {
            // the room server is available, send message
            RoomServerInterface roomServer = (RoomServerInterface) registry.lookup(addr);
            roomServer.receiveMsg(username, msg, roomID);
            ServerHelper.writeDefaultSuccessRsp(exchange);
            return;
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
        }

        // the room server is not available, try other room servers
        int roomServerID = Integer.parseInt(addr.substring(addr.length() - 1));
        int trials = 0;
        while(trials < ServerConfig.ROOM_CLUSTER_SIZE) {
            try {
                roomServerID = (roomServerID + 1) % ServerConfig.ROOM_CLUSTER_SIZE;
                RoomServerInterface roomServer = (RoomServerInterface) registry.lookup(ServerConfig.RPC_ROOM_NAME + roomServerID);
                roomServer.relocateRoom(roomID);
                roomServer.receiveMsg(username, msg, roomID);
                roomIDToAddr.put(roomID, ServerConfig.RPC_ROOM_NAME + roomServerID);
                ServerHelper.writeDefaultSuccessRsp(exchange);
            } catch (NotBoundException | RemoteException e) {
                e.printStackTrace();
                trials++;
            }
        }
        ServerHelper.writeServerErrorRsp(exchange);
    }
}
