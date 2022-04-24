package server.room;

import config.GlobalConfig;
import config.Log;
import server.config.DBHelper;
import server.config.DBRsp;
import server.config.ServerConfig;
import server.route.RouteServerInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class RoomServer implements RoomServerInterface {
    private static DBHelper dbHelper;
    private static final String host = "127.0.0.1";
    private static final int roomServerPortNumberStarting = 4000;
    private static final int roomIDUpperBound = 9999;
    private static int roomServerID = 0;
    private static Registry registry;

    public RoomServer() throws RemoteException, NotBoundException {
        dbHelper = new DBHelper();
        registry = LocateRegistry.getRegistry(host);
        RoomServerInterface stub = (RoomServerInterface) UnicastRemoteObject.exportObject(this, 0);
        registry.rebind(ServerConfig.RPC_ROOM_NAME, stub);
    }

    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                roomServerID = Integer.parseInt(args[0]);
            } else {
                roomServerID = 0;
            }
            RoomServer roomServer = new RoomServer();
            Log.Info("roomServer " + roomServerID + " is running...");
        } catch (NotBoundException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int createRoom(String username) throws RemoteException {
        String roomAddress = ServerConfig.RPC_ROOM_NAME + roomServerID;
        //generate random roomID from 0-roomIDUpperBound
        Random rand = new Random();
        int roomID = rand.nextInt(roomIDUpperBound);
        // check if roomID already exists
        DBRsp roomIDExistCheckRsp = dbHelper.getRoomAddress(roomID);
        // until find a new roomID
        while (roomIDExistCheckRsp.getResCode() != ServerConfig.ERROR_NO_EXIST) {
            roomID = rand.nextInt(roomIDUpperBound);
            roomIDExistCheckRsp = dbHelper.getRoomAddress(roomID);
        }

        // after room is created successfully, add user to roomUserList, add room to userRoomList
        if (dbHelper.updateRoomAddress(roomID, roomAddress) == ServerConfig.SUCCESS) {
            if (dbHelper.addUserToRoom(roomID, username) == ServerConfig.SUCCESS) {
                if (dbHelper.addRoomToUser(username, roomID) == ServerConfig.SUCCESS) {
                    return roomID;
                } else {
                    Log.Error("Error when adding roomID " + roomID + " to user " + username + "'s room list.");
                    throw new RemoteException();
                }
            } else {
                Log.Error("Error when adding user " + username + " to room " + roomID);
                throw new RemoteException();
            }
        } else {
            Log.Error("Error when creating new room with roomID " + roomID);
            throw new RemoteException();
        }
    }

    @Override
    public void relocateRoom(int roomID) throws RemoteException {
        // update new roomAddress
        dbHelper.updateRoomAddress(roomID, String.valueOf(roomServerID));
    }

    @Override
    public int receiveInvitation(String fromUser, String toUser, int roomID) throws RemoteException, NotBoundException {
        // check if the invitation is valid(if toUser exists and not in the room)
        if (dbHelper.checkUsername(toUser) == ServerConfig.SERVER_ERROR) {
            Log.Error("The invitation recipient does not exist with userID " + toUser);
            return GlobalConfig.NO_MATCH;
        } else if (dbHelper.getRoomAddress(roomID).getResCode() == ServerConfig.ERROR_NO_EXIST) {
            Log.Error("The invitation room does not exist with roomID " + roomID);
            return GlobalConfig.NO_ROOM;
        } else if (dbHelper.getRoomUserList(roomID).contains(toUser)) {
            Log.Error("The invitation recipient " + toUser + " is already in the room " + roomID);
            return GlobalConfig.DUP_USER;
        } else {
            // invitation is valid, redirect message to route server
            RouteServerInterface routeServer = (RouteServerInterface) registry.lookup(ServerConfig.RPC_ROUTE_NAME);
            routeServer.sendMsgToClient(toUser, "You received a new invitation from " + fromUser);
            return GlobalConfig.SUCCESS;
        }
    }

    @Override
    public int receiveInvitationRsp(String fromUser, int roomID, boolean accept) throws RemoteException, NotBoundException {
        // receive invitation response from RouteServer(user)
        // add the roomID-username pair to database if invitation accepted
        if (accept) {
            // check if user can be added to the room
            if (!dbHelper.getRoomUserList(roomID).contains(fromUser)) {
                if (dbHelper.getInvitationHistory(fromUser).contains(String.valueOf(roomID))){
                    dbHelper.addUserToRoom(roomID, fromUser);
                    // delete invitation history
                    dbHelper.deleteInvitationHistory(fromUser, roomID);
                    return GlobalConfig.SUCCESS;
                } else {
                    Log.Error("The invitation to room "+ roomID+" does not exist in " + fromUser + " invitation history ");
                    return GlobalConfig.NO_INVITATION;
                }
            } else {
                Log.Error("The invitation recipient " + fromUser + " is already in the room " + roomID);
                return GlobalConfig.DUP_USER;
            }
        } else {
            // delete invitation history
            if (dbHelper.deleteInvitationHistory(fromUser, roomID)==ServerConfig.SUCCESS){
                return GlobalConfig.DECLINE;
            } else {
                return GlobalConfig.SERVER_ERROR;
            }

        }
    }

    @Override
    public void receiveMsg(String fromUser, String msg, int roomID) throws RemoteException, NotBoundException {
        // get all users in the list
        ArrayList<String> roomUserList = dbHelper.getRoomUserList(roomID);
        // for each user in the room, send message via routeServer
        RouteServerInterface routeServer = (RouteServerInterface) registry.lookup(ServerConfig.RPC_ROUTE_NAME);
        for (String toUser : roomUserList) {
            routeServer.sendMsgToClient(toUser, msg);
        }
    }
}
