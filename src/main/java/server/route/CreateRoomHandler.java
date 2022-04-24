package server.route;

import com.sun.net.httpserver.HttpExchange;
import config.GlobalConfig;
import config.Log;
import org.json.JSONObject;
import server.config.DBHelper;
import server.config.ServerConfig;
import server.config.ServerHelper;
import server.room.RoomServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class CreateRoomHandler extends AbsRouteSvrHandler {
    // the ID of the RoomServer to assign at this round
    private int roomServerID;

    public CreateRoomHandler(DBHelper dbHelper, Registry registry) {
        super(dbHelper, registry);
        roomServerID = 1;
    }

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
                roomServerID = (roomServerID + 1) % ServerConfig.ROOM_CLUSTER_SIZE;
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
