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

    private String userRoomListKey(String username) {
        return "userRoomList." + username;
    }

    // invitation list user received
    private String userInvitationListKey(String username) {
        return "userInvitationList." + username;
    }

    public int addNewUser(String username, String password) {
        try {
            DBReq reqBody = new DBReq(UsernameKey(username), password, ServerConfig.ACTION_PUT);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch (RemoteException exp) {
            Log.Error("Error " + exp.getMessage() + " when adding new user " + username);
            return ServerConfig.SERVER_ERROR;
        }
    }

    public int updateRoomAddress(int roomID, String address) {
        try {
            DBReq reqBody = new DBReq(roomAddrKey(roomID), address, ServerConfig.ACTION_PUT);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch (RemoteException exp) {
            Log.Error("Error " + exp.getMessage() + " when updating room address with roomID " + roomID + " and address " + address);
            return ServerConfig.SERVER_ERROR;
        }
    }

    public int addUserToRoom(int roomID, String username) {
        try {
            DBReq reqBody = new DBReq(roomUserListKey(roomID), username, ServerConfig.ACTION_PUT, true);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch (RemoteException e) {
            Log.Error("Error " + e.getMessage() + " when adding user " + username + " to room " + roomID);
            return ServerConfig.SERVER_ERROR;
        }
    }

    public int addRoomToUser(String username, int roomID) {
        try {
            DBReq reqBody = new DBReq(userRoomListKey(username), String.valueOf(roomID), ServerConfig.ACTION_PUT, true);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch (RemoteException e) {
            Log.Error("Error " + e.getMessage() + " when adding room " + roomID + " to user " + username + "'s room list ");
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
        DBReq reqBody = new DBReq(roomUserListKey(roomID), ServerConfig.ACTION_GET);
        DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
        return rspBody.getValue();
    }

    public ArrayList<String> getUserRoomList(String username) throws RemoteException {
        DBReq reqBody = new DBReq(userRoomListKey(username), ServerConfig.ACTION_GET);
        DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
        return rspBody.getValue();
    }

    public int checkUsername(String username) {
        try {
            DBReq reqBody = new DBReq(UsernameKey(username), ServerConfig.ACTION_GET);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch (RemoteException exp) {
            return ServerConfig.SERVER_ERROR;
        }
    }

    public boolean matchUsernamePassword(String username, String password) {
        try {
            DBReq reqBody = new DBReq(UsernameKey(username), ServerConfig.ACTION_GET);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return password.equals(rspBody.getValue().get(0));
        } catch (RemoteException e) {
            Log.Error("Error when checking password match for user " + username);
            return false;
        }
    }

    public int deleteInvitationHistory(String username, int roomID) {
        try {
            ArrayList<String> invitationHistory = getInvitationHistory(username);
            invitationHistory.remove(String.valueOf(roomID));
            return updateInvitationHistory(username, roomID, false);
        } catch (Exception e) {
            Log.Error("Error " + e.getMessage() + " when deleting invitation of " + roomID + " to user " + username + "'s room list ");
            return ServerConfig.SERVER_ERROR;
        }
    }

    public int updateInvitationHistory(String username, int roomID, boolean append) {
        try {
            DBReq reqBody = new DBReq(userInvitationListKey(username), String.valueOf(roomID), ServerConfig.ACTION_PUT, append);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch (RemoteException e) {
            Log.Error("Error " + e.getMessage() + " when adding invitation of " + roomID + " to user " + username + "'s room list ");
            return ServerConfig.SERVER_ERROR;
        }
    }

    public ArrayList<String> getInvitationHistory(String username) throws RemoteException {
        DBReq reqBody = new DBReq(userInvitationListKey(username), ServerConfig.ACTION_GET);
        DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
        return rspBody.getValue();
    }
}
