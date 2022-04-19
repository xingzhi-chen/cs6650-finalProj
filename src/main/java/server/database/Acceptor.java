package server.database;

import config.GlobalConfig;
import config.Log;
import server.database.config.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class Acceptor implements RMIAcceptorInterface {
    // the current promised proposal ID of the key
    private final HashMap<String, Long> promisedIDs = new HashMap<>();
    // the latest accepted proposal ID of the key
    private final HashMap<String, Long> acceptedIDs = new HashMap<>();
    // the latest accepted proposal value of the key
    private final HashMap<String, Proposal> acceptedProposals = new HashMap<>();
    // the server object the acceptor is on
    private final DBServer myServer;
    // the local log file for restoring data after crash
    private final String logFile;
    // lock for synchronized operations of the three proposal record HashMaps above
    private final ReentrantLock lock = new ReentrantLock();

    public Acceptor(DBServer myServer) {
        // bind the acceptor to its server
        this.myServer = myServer;
        this.logFile = String.format("./acceptor%d.txt", myServer.getServerID());
    }

    @Override
    public String PAXOSPrepare(String proposalJSONStr) throws RemoteException {
        Proposal proposal = new Proposal(proposalJSONStr);
        long proposalID = proposal.getProposalID();
        // key of the data the proposal is editing
        String pKey = proposal.getReqBody().getKey();
        Log.Debug("Acceptor %d receive prepare request for proposalID %d of key %s", myServer.getServerID(), proposalID, pKey);
        String rsp = null;
        lock.lock();
        if (!promisedIDs.containsKey(pKey) || promisedIDs.get(pKey) < proposalID) {
            // this is the latest proposal the acceptor ever sees for this key
            Log.Debug("Acceptor %d promised for proposalID %d", myServer.getServerID(), proposalID);
            // promised to the proposal ID
            promisedIDs.put(pKey, proposalID);
            // include previously accepted proposal ID and value in the result
            if (acceptedIDs.containsKey(pKey)) {
                Log.Debug("Acceptor %d has accepted proposalID %d of key %s", myServer.getServerID(), acceptedIDs.get(pKey), pKey);
                rsp = new PAXOSPrepareResult(acceptedIDs.get(pKey), acceptedProposals.get(pKey), DBConfig.PROMISED).toJSONString();
            } else {
                rsp = new PAXOSPrepareResult(0, null, DBConfig.PROMISED).toJSONString();
            }
        } else {
            // this is not the latest proposal, deny it
            Log.Warn("Acceptor %d has promised to proposalID %d of key %s", myServer.getServerID(), promisedIDs.get(pKey), pKey);
            rsp = new PAXOSPrepareResult(0, null, DBConfig.DENIED).toJSONString();
        }
        lock.unlock();
        return rsp;
    }

    @Override
    public int PAXOSAccept(String proposalJSONStr) throws RemoteException {
        Proposal proposal = new Proposal(proposalJSONStr);
        long proposalID = proposal.getProposalID();
        String pKey = proposal.getReqBody().getKey();
        Log.Debug("Acceptor %d accepts proposalID %d of key %s", myServer.getServerID(), proposalID, pKey);
        int res;
        lock.lock();
        if (!promisedIDs.containsKey(pKey) || promisedIDs.get(pKey) <= proposalID) {
            // this is the latest proposal the acceptor ever sees for the key
            try {
                // process the request on local server database
                myServer.processReq(proposal);
                // add it to the proposal record
                promisedIDs.put(pKey, proposalID);
                acceptedIDs.put(pKey, proposalID);
                acceptedProposals.put(pKey, proposal);
                // write accepted proposal to local log file
                FileWriter writer = new FileWriter(logFile, true);
                writer.write(proposalJSONStr + "\n");
                writer.close();
                // notify learners about the result
                for(int i = 1; i <= DBConfig.LEARNER_SIZE; i++) {
                    try {
                        RMILearnerInterface learner = (RMILearnerInterface) myServer.getRegistry().lookup(DBConfig.RPC_LEARNER_NAME + i);
                        learner.notifyAcceptance(proposal.toJSONString(), myServer.getServerID());
                    } catch (NotBoundException | RemoteException exp) {
                        Log.Error("acceptor %d failed to notify learner %d: %s", myServer.getServerID(), i, exp.getMessage());
                    }
                }
                res = DBConfig.ACCEPTED;
            } catch (IOException exp) {
                Log.Error("Acceptor %d fail to process proposal %s", myServer.getServerID(), proposalJSONStr);
                exp.printStackTrace();
                res = DBConfig.DENIED;
            }
        } else {
            // this is not the latest proposal the acceptor ever sees, deny it
            res = DBConfig.DENIED;
        }
        lock.unlock();
        return res;
    }

    public static void main(String[] args) {
        if (args.length < 1 || args[0].length() == 0) {
            Log.Error("command line format: java paxos.Acceptor {serverID} {failureType:timeout/crash}");
            System.exit(0);
        }

        DBServer acceptor = new DBServer(Integer.parseInt(args[0]), DBConfig.RPC_ACCEPTOR_NAME);
    }
}
