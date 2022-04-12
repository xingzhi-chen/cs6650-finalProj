package server.route;

import java.rmi.Remote;

public interface RouteServerInterface extends Remote {
    /*
     * receive message from inner servers (RoomServer), forward it to users through websocket
     * @param String toUser: the destination user of the message
     * @param String: the message from the inner server
     */
    void sendMsgToClient(String toUser, String msg);
}
