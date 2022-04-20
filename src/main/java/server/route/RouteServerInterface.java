package server.route;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RouteServerInterface extends Remote {
    /*
     * receive message from inner servers (RoomServer), forward it to users through websocket
     * @param String toUser: the destination user of the message
     * @param String: the message from the inner server
     */
    void sendMsgToClient(String toUser, String msg) throws RemoteException;
}
