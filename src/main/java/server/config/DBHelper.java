package server.config;

import config.Log;
import server.database.DBInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

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

    private String roomUserListKey(int roomID) {
        return "roomUserList." + roomID;
    }

    public int updateRoomAddress(int roomID, String address) {
        try {
            DBReq reqBody = new DBReq(roomAddrKey(roomID), address, ServerConfig.ACTION_PUT);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch (RemoteException exp) {
            Log.Error("Error "+exp.getMessage()+" when updating room address with roomID "+roomID+" and address "+address);
            return ServerConfig.SERVER_ERROR;
        }
    }

    public int addUserToRoom(int roomID, String username) {
        try {
            DBReq reqBody = new DBReq(roomUserListKey(roomID), username, ServerConfig.ACTION_PUT, true);
            // todo arraylist?
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch (RemoteException e) {
            Log.Error("Error "+e.getMessage()+" when adding user "+username+" to room "+roomID);
            return ServerConfig.SERVER_ERROR;
        }
    }

    public DBRsp getRoomAddress(int roomID) {
        try {
            DBReq reqBody = new DBReq(roomAddrKey(roomID), ServerConfig.ACTION_GET);
            return new DBRsp(db.DBRequest(reqBody.toJSONString()));
        } catch (RemoteException exp) {
            return new DBRsp(ServerConfig.SERVER_ERROR, ServerConfig.errorMsg.get(ServerConfig.SERVER_ERROR));
        }
    }

    public ArrayList<String> getRoomUserList(int roomID) throws RemoteException {
        // todo: what value to use when perform get all
        DBReq reqBody = new DBReq(roomUserListKey(roomID), null, ServerConfig.ACTION_GET, true);
        DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
        return rspBody.getValue();
    }

    public int checkUsername(String username) {
        try {
            DBReq reqBody = new DBReq(username, ServerConfig.ACTION_GET);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch (RemoteException exp) {
            return ServerConfig.SERVER_ERROR;
        }
    }
}
