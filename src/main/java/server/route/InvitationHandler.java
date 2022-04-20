package server.route;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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

public class InvitationHandler extends AbsRouteSvrHandler {
    public InvitationHandler(DBHelper dbHelper, Registry registry) {
        super(dbHelper, registry);
    }

    @Override
    public void processReq(HttpExchange exchange, JSONObject body, String username) {
        int roomID = body.getInt(GlobalConfig.ROOM_ID);
        String newUser = body.getString(GlobalConfig.NEW_USER);

        DBRsp dbRsp = dbHelper.getRoomAddress(roomID);
        if (dbRsp.getResCode() == ServerConfig.ERROR_NO_EXIST) {
            ServerHelper.writeNoRoomRsp(exchange);
            return;
        }
        String addr = dbRsp.getValue().get(0);

        try {
            // the room server is available, send message
            RoomServerInterface roomServer = (RoomServerInterface) registry.lookup(addr);
            roomServer.receiveInvitation(username, newUser, roomID);
            ServerHelper.writeDefaultSuccessRsp(exchange);
            return;
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
        }
        ServerHelper.writeServerErrorRsp(exchange);
    }
}
