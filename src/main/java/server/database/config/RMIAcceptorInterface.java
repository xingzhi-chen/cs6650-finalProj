package server.database.config;

import java.rmi.Remote;
import java.rmi.RemoteException;

// acceptor RMI interface
public interface RMIAcceptorInterface extends Remote {
    /*
     * the acceptor prepare for a proposal
     * @param String proposalJSONStr: a JSON string of the proposal
     * @return: a JSON string of the resulting PAXOSPrepareResult class
     */
    String PAXOSPrepare(String proposalJSONStr) throws RemoteException;
    /*
     * the acceptor accept a proposal
     * @param String proposalJSONStr: a JSON string of the proposal
     * @return result code of Config.ACCEPTED/Config.DENIED
     */
    int PAXOSAccept(String proposalJSONStr) throws RemoteException;
}
