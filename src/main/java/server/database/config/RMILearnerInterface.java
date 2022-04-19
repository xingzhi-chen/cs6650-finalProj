package server.database.config;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMILearnerInterface extends Remote {
    /*
     * the learner receives a proposal acceptance notice from an acceptor
     * @param String proposalJSONStr: a JSON string of the proposal
     * @param acceptorID: the acceptor that sends the notice
     */
    void notifyAcceptance(String proposalJSONStr, int acceptorID) throws RemoteException;
}
