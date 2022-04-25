package server.route;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.GlobalConfig;
import config.Log;
import org.json.JSONObject;
import server.config.DBHelper;
import server.config.DBRsp;
import server.config.ServerConfig;
import server.config.ServerHelper;
import server.room.RoomServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class InvitationHandler extends AbsRouteSvrHandler {
    public InvitationHandler(DBHelper dbHelper, Registry registry) {
        super(dbHelper, registry);
    }

    @Override
    public void processReq(HttpExchange exchange, JSONObject body, String username) {
        if (!body.has(GlobalConfig.ROOM_ID) || !body.has(GlobalConfig.NEW_USER)) {
            ServerHelper.writeWrongReqRsp(exchange);
            return;
        }

        int roomID = body.getInt(GlobalConfig.ROOM_ID);
        String newUser = body.getString(GlobalConfig.NEW_USER);
        DBRsp dbRsp = dbHelper.getRoomAddress(roomID);
        Log.Info("receive invitation request for room %d from user %s to user %s", roomID, username, newUser);
        if (dbRsp.getResCode() == ServerConfig.ERROR_NO_EXIST) {
            ServerHelper.writeNoRoomRsp(exchange);
            return;
        }
        String addr = dbRsp.getValue().get(0);

        try {
            // the room server is available, send message
            RoomServerInterface roomServer = (RoomServerInterface) registry.lookup(addr);
            int resCode = roomServer.receiveInvitation(username, newUser, roomID);
            switch(resCode) {
                case GlobalConfig.NO_MATCH: ServerHelper.writeNoMatchRsp(exchange); break;
                case GlobalConfig.NO_ROOM: ServerHelper.writeNoRoomRsp(exchange); break;
                case GlobalConfig.DUP_USER: ServerHelper.writeDupUserRoomRsp(exchange); break;
                default: ServerHelper.writeDefaultSuccessRsp(exchange); break;
            }
            return;
        } catch (NotBoundException | RemoteException e) {
            Log.Error("fail to connect to %s when processing invitation request, error: %s", addr, e.getMessage());
        }
        ServerHelper.writeServerErrorRsp(exchange);
    }
}
