package server.room;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RoomServerInterface extends Remote {
    /*
     * receive createRoom request from RouteServer(user), create a room and return the randomly generated roomID, record user-room pair into db
     * @param String username: the user requested to create a room
     */
    int createRoom(String username) throws RemoteException;
    /*
     * an existing chat room is relocated to the server, probably due to crash of another server, overwrite roomID-serverAddress pair into db
     * @param int roomID: the ID of the existing room to be relocated
     */
    void relocateRoom(int roomID) throws RemoteException;
    /*
     * receive invitation from RouteServer(user), check if the invitation is legal(toUser exists and not in the room), forward this message to RouteServer
     * @param String fromUser: the user that sends invitation
     * @param String toUser: the user that sends invitation to
     * @param int roomID: the room ID of the invitation regards to
     */
    void receiveInvitation(String fromUser, String toUser, int roomID) throws RemoteException;
    /*
     * receive invitation response from RouteServer(user), add the roomID-username pair to database
     * @param String fromUser: the user that receives and responds to the invitation
     * @param int roomID: the room ID of the invitation regards to
     */
    void receiveInvitationRsp(String fromUser, int roomID, boolean accept) throws RemoteException;
    /*
     * receive a chatting message from RouteServer(user), broadcast it to all users in the room and forward the broadcasting messages to RouteServer
     * @param String fromUser: the user that sends invitation
     * @param String msg: the message from the user
     * @param int roomID: the room ID of the message
     */
    void receiveMsg(String fromUser, String msg, int roomID) throws RemoteException;
}
