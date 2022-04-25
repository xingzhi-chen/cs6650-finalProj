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

public class InvitationRspHandler extends AbsRouteSvrHandler {
    public InvitationRspHandler(DBHelper dbHelper, Registry registry) {
        super(dbHelper, registry);
    }

    @Override
    public void processReq(HttpExchange exchange, JSONObject body, String username) {
        if (!body.has(GlobalConfig.ROOM_ID)) {
            ServerHelper.writeWrongReqRsp(exchange);
            return;
        }
        int roomID = body.getInt(GlobalConfig.ROOM_ID);
        boolean accept = body.getBoolean(GlobalConfig.ACCEPT);

        DBRsp dbRsp = dbHelper.getRoomAddress(roomID);
        if (dbRsp.getResCode() == ServerConfig.ERROR_NO_EXIST) {
            ServerHelper.writeNoRoomRsp(exchange);
            return;
        }
        String addr = dbRsp.getValue().get(0);

        try {
            // the room server is available, send message
            RoomServerInterface roomServer = (RoomServerInterface) registry.lookup(addr);
            int resCode = roomServer.receiveInvitationRsp(username, roomID, accept);
            ServerHelper.writeDefaultSuccessRsp(exchange);
            return;
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
        }
        ServerHelper.writeServerErrorRsp(exchange);
    }
}
