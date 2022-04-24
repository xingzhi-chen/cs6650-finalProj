package server.config;

import server.database.DBInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DBHelper {
    private DBInterface db;

    public DBHelper() throws RemoteException, NotBoundException {
        String host = "127.0.0.1";
        Registry registry = LocateRegistry.getRegistry(host);
        int serverID = 1;
        db = (DBInterface) registry.lookup(ServerConfig.RPC_DB_NAME + serverID);
    }

    private String roomAddrKey(int roomID) {
        return "roomAddr." + roomID;
    }

    private String UsernameKey(String username) {
        return "username." + username;
    }

    public int updateRoomAddress(int roomID, String address) {
        try {
            DBReq reqBody = new DBReq(roomAddrKey(roomID), address, ServerConfig.ACTION_PUT);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch(RemoteException exp) {
            return ServerConfig.SERVER_ERROR;
        }
    }

    public DBRsp getRoomAddress(int roomID) {
        try {
            DBReq reqBody = new DBReq(roomAddrKey(roomID), ServerConfig.ACTION_GET);
            return new DBRsp(db.DBRequest(reqBody.toJSONString()));
        } catch(RemoteException exp) {
            return new DBRsp(ServerConfig.SERVER_ERROR, ServerConfig.errorMsg.get(ServerConfig.SERVER_ERROR));
        }
    }

    public int checkUsername(String username) {
        try {
            DBReq reqBody = new DBReq(UsernameKey(username), ServerConfig.ACTION_GET);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch(RemoteException exp) {
            return ServerConfig.SERVER_ERROR;
        }
    }
}
