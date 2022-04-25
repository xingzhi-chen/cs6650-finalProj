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

public class GetChatHistoryHandler extends AbsRouteSvrHandler {
    public GetChatHistoryHandler(DBHelper dbHelper, Registry registry) {
        super(dbHelper, registry);
    }

    @Override
    public void processReq(HttpExchange exchange, JSONObject body, String username) {
        if (!body.has(GlobalConfig.ROOM_ID)) {
            ServerHelper.writeWrongReqRsp(exchange);
            return;
        }

        int roomID = body.getInt(GlobalConfig.ROOM_ID);
        DBRsp dbRsp = dbHelper.getRoomAddress(roomID);
        if (dbRsp.getResCode() == ServerConfig.ERROR_NO_EXIST) {
            ServerHelper.writeNoRoomRsp(exchange);
            return;
        }
        String addr = dbRsp.getValue().get(0);

        try {
            // the room server is available, send message
            RoomServerInterface roomServer = (RoomServerInterface) registry.lookup(addr);
            String history = roomServer.getChatHistory(roomID);
            String rsp = new JSONObject()
                    .put(GlobalConfig.RES_CODE, GlobalConfig.SUCCESS)
                    .put(GlobalConfig.MESSAGE, GlobalConfig.errorMsg.get(GlobalConfig.SUCCESS))
                    .put(GlobalConfig.HISTORY, history)
                    .toString();
            return;
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
        }
        ServerHelper.writeServerErrorRsp(exchange);
    }
}
