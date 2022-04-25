package server.config;

import config.GlobalConfig;
import config.Log;
import server.database.DBInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;

/*
* helper functions for db operations
 */
public class DBHelper {
    private DBInterface db;

    // randomly choose a db proposer as the stub
    public DBHelper() throws RemoteException, NotBoundException {
        String host = "127.0.0.1";
        Registry registry = LocateRegistry.getRegistry(host);
        int serverID = (new Random().nextInt() % ServerConfig.DB_CLUSTER_SIZE + ServerConfig.DB_CLUSTER_SIZE) % ServerConfig.DB_CLUSTER_SIZE + 1;
        db = (DBInterface) registry.lookup(ServerConfig.RPC_DB_NAME + serverID);
    }

    // key for address of a room
    private String roomAddrKey(int roomID) {
        return "roomAddr." + roomID;
    }

    // key for password of a username
    private String UsernameKey(String username) {
        return "username." + username;
    }

    // key for user list of a room
    private String roomUserListKey(int roomID) {
        return "roomUserList." + roomID;
    }

    // key for room list of a user
    private String userRoomListKey(String username) {
        return "userRoomList." + username;
    }

    // add a new user with his/her password to the system
    public int addNewUser(String username, String password) {
        try {
            DBReq reqBody = new DBReq(UsernameKey(username), password, ServerConfig.ACTION_PUT);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            Log.Info("add new user %s to system", username);
            return rspBody.getResCode();
        } catch (RemoteException exp) {
            Log.Error("Error " + exp.getMessage() + " when adding new user " + username);
            return ServerConfig.SERVER_ERROR;
        }
    }

    // update the address of a room
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

    // add new user to a room
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

    // add roomID to user's room list
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

    // get address (which RoomServer the room is on) of a room
    public DBRsp getRoomAddress(int roomID) {
        try {
            DBReq reqBody = new DBReq(roomAddrKey(roomID), ServerConfig.ACTION_GET);
            return new DBRsp(db.DBRequest(reqBody.toJSONString()));
        } catch (RemoteException exp) {
            return new DBRsp(ServerConfig.SERVER_ERROR, ServerConfig.errorMsg.get(ServerConfig.SERVER_ERROR));
        }
    }

    // get the users of a room
    public ArrayList<String> getRoomUserList(int roomID) throws RemoteException {
        DBReq reqBody = new DBReq(roomUserListKey(roomID), ServerConfig.ACTION_GET);
        DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
        return rspBody.getValue();
    }

    // get the rooms a user is in
    public ArrayList<String> getUserRoomList(String username) throws RemoteException {
        DBReq reqBody = new DBReq(userRoomListKey(username), ServerConfig.ACTION_GET);
        DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
        return rspBody.getValue();
    }

    // check if username exists
    public int checkUsername(String username) {
        try {
            DBReq reqBody = new DBReq(UsernameKey(username), ServerConfig.ACTION_GET);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch (RemoteException exp) {
            return ServerConfig.SERVER_ERROR;
        }
    }

    // check if the password matches the one in the record
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

    // get the ID of the RouteServer that is acting as the master
    public int getMasterRouteID() {
        try {
            DBReq reqBody = new DBReq(ServerConfig.RPC_ROUTE_NAME, ServerConfig.ACTION_GET);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            if (rspBody.getResCode() == ServerConfig.ERROR_NO_EXIST) {
                return ServerConfig.ERROR_NO_EXIST;
            } else {
                return Integer.parseInt(rspBody.getValue().get(0));
            }
        } catch (RemoteException e) {
            Log.Error("fail to connect to db when reading master route ID");
            return ServerConfig.SERVER_ERROR;
        }
    }

    // mark RouteServer of ID myID as the master server
    public int updateMasterRouteID(Integer myID) {
        try {
            DBReq reqBody = new DBReq(ServerConfig.RPC_ROUTE_NAME, myID.toString(), ServerConfig.ACTION_PUT, false, 60);
            DBRsp rspBody = new DBRsp(db.DBRequest(reqBody.toJSONString()));
            return rspBody.getResCode();
        } catch (RemoteException e) {
            Log.Error("fail to connect to db when updating master route ID to %d", myID);
            return ServerConfig.SERVER_ERROR;
        }
    }
}
