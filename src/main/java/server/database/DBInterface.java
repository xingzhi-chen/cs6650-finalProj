package server.database;

import java.rmi.Remote;
import java.rmi.RemoteException;

// RMI interface for client-server database operation
public interface DBInterface extends Remote {
    /*
    * database operation request
    * @param String req: a JSON string of client request
     */
    String DBRequest(String req) throws RemoteException;
}
