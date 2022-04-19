package server.database;

import config.GlobalConfig;
import config.Log;
import server.database.config.DBConfig;
import server.database.config.Proposal;
import server.database.config.RMILearnerInterface;

import java.io.IOException;
import java.rmi.RemoteException;

public class Learner implements RMILearnerInterface {
    // the server object the learner is on
    private final DBServer myServer;
    public Learner(DBServer myServer) {
        // bind the acceptor to its server
        this.myServer = myServer;
    }

    @Override
    public void notifyAcceptance(String proposalJSONStr, int acceptorID) throws RemoteException {
        Proposal proposal = new Proposal(proposalJSONStr);
        Log.Debug("Learner %d receives accept notification from acceptor %d: %s", myServer.getServerID(), acceptorID, proposalJSONStr);
        try {
            myServer.getLock().lock();
            if (myServer.isNewAcceptance(proposal)) {
                // the proposal is the latest one the learner received for the key, add a new record and start counting
                myServer.addNewAcceptance(proposal);
                myServer.addCurrentAcceptance(proposal, acceptorID);
            } else if (myServer.isCurrentAcceptance(proposal)) {
                // the proposal matches the current one (also the latest one) received for the key, add to count
                myServer.addCurrentAcceptance(proposal, acceptorID);
            }
            // the number of acceptance received reaches majority, process the proposal
            if (myServer.getAcceptanceNum(proposal) == DBConfig.QUORUM) {
                Log.Debug("Quorum reached at Learner %d, process proposal %s", myServer.getServerID(), proposalJSONStr);
                myServer.processReq(proposal);
            }
        } catch (IOException exp) {
            Log.Error("Learner %d fails to process proposal %s", myServer.getServerID(), proposalJSONStr);
            exp.printStackTrace();
        } finally {
            myServer.getLock().unlock();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1 || args[0].length() == 0) {
            Log.Error("command line format: java paxos.Acceptor {port} {serverID}");
            System.exit(0);
        }

        DBServer server = new DBServer(Integer.parseInt(args[0]), DBConfig.RPC_LEARNER_NAME);
    }
}
