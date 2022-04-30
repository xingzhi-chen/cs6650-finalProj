package client.comm;

public interface ClientCommInterface {
    /*
     * send register request to login server, return a token if succeed, store the token to local memory
     * @param String username: username to register, read from user input
     * @param String password: password to the username
     */
    void register(String username, String password);
    /*
     * send login request to login server, return a token if succeed, store the token to local memory
     * @param String username: username of the user, read from user input
     * @param String password: password of the username, read from user input
     */
    void login(String username, String password);
    /*
     * create a websocket connection to route server for server message
     * @param String token: token from login
     */
    void websocketConnection(String token);
    /*
     * create a new chat room, a randomly generated room ID will be returned through HTTP response
     * @param String token: token from login
     */
    void createRoom(String token);
    /*
     * send room invitation to another user, no response other than network ACK
     * @param String token: token from login
     * @param String otherUsername: the user to invite
     * @param int roomID: the room of the invitation
     */
    void sendInvitation(String token, String otherUsername, int roomID);
    /*
     * send room invitation response to server, no response other than network ACK
     * @param String token: token from login
     * @param int roomID: the room of the invitation
     * @param boolean accept: the response to invitation
     */
    void sendInvitationRsp(String token, int roomID, boolean accept);
    /*
     * send message to a room, no response other than network ACK
     * @param String token: token from login
     * @param String message: message to send
     * @param int roomID: the room to send message
     */
    void sendMessage(String token, String message, int roomID);
}
