package server.database;

import config.Log;
import server.config.*;
import server.database.config.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

public class DBServer extends AbsDBServer implements DBInterface {
    // ID to identify acceptor/proposer/learner on the server
    private final int serverID;
    // RMI registry on the machine
    private Registry registry;
    // possible proposer on this server
    private Proposer proposer;
    // possible acceptor on this server
    private Acceptor acceptor;
    // possible learner on this server
    private Learner learner;
    // lock for synchronized operation of proposal data
    private final ReentrantLock paxosLock = new ReentrantLock();

    // the timestamp of the last received acceptance of the key
    private final HashMap<String, Long> proposalLastAccepted = new HashMap<>();
    // the number of acceptance received of the last received acceptance of the key
    private final HashMap<String, HashSet<Integer>> proposalAcceptedCount = new HashMap<>();
    // the timestamp at and response result of which the key was last modified (consensus was reached of the request regarding the key)
    private final HashMap<String, HashMap<Long, String>> proposalLastProcessed = new HashMap<>();

    // bind the acceptor object of the server to the rmi registry
    private void bindAcceptor(int serverID) throws RemoteException {
        acceptor = new Acceptor(this);
        // register RMI method
        RMIAcceptorInterface stub = (RMIAcceptorInterface) UnicastRemoteObject.exportObject(acceptor, 0);
        // use "RMIDBServer+{serverID}" to register a server in rmiregistry
        registry.rebind(DBConfig.RPC_ACCEPTOR_NAME + this.serverID, stub);
        Log.Info("Acceptor %d is working...", serverID);
    }

    // bind the learner object of the server to the rmi registry
    private void bindLearner(int serverID) throws RemoteException {
        learner = new Learner(this);
        // register RMI method
        RMILearnerInterface stub = (RMILearnerInterface) UnicastRemoteObject.exportObject(learner, 0);
        // use "RMIDBServer+{serverID}" to register a server in rmiregistry
        registry.rebind(DBConfig.RPC_LEARNER_NAME + this.serverID, stub);
        Log.Info("Learner %d is working...", serverID);
    }

    // add proposer logic to the server, bind the server itself to the rmi registry as the database server
    private void bindProposer(int serverID) throws RemoteException {
        proposer = new Proposer(this);
        // register RMI method
        DBInterface stub = (DBInterface) UnicastRemoteObject.exportObject(this, 0);
        // use "RMIDBServer+{serverID}" to register a server in rmiregistry
        registry.rebind(DBConfig.RPC_DB_NAME + this.serverID, stub);
        Log.Info("Proposer %d is working...", serverID);
    }

    public DBServer(int serverID, String PAXOSType) {
        this.serverID = serverID;
        try {
            // set timeout
            System.setProperty("sun.rmi.transport.tcp.responseTimeout", String.valueOf(DBConfig.DB_CLUSTER_TIMEOUT));
            System.setProperty("sun.rmi.dgc.ackTimeout", String.valueOf(DBConfig.DB_CLUSTER_TIMEOUT));
            System.setProperty("sun.rmi.transport.connectionTimeout", String.valueOf(DBConfig.DB_CLUSTER_TIMEOUT));
            registry = LocateRegistry.getRegistry();
            if (PAXOSType.equals(DBConfig.RPC_ACCEPTOR_NAME)) {
                // add acceptor if this is an acceptor server
                bindAcceptor(serverID);
            } else if (PAXOSType.equals(DBConfig.RPC_DB_NAME)) {
                // add both proposer and learner if the server is a proposer server
                bindProposer(serverID);
                bindLearner(serverID);
            } else if (PAXOSType.equals(DBConfig.RPC_LEARNER_NAME)) {
                // add learner if this is a learner server
                bindLearner(serverID);
            }
        } catch (RemoteException exp) {
            Log.Error("fail to create RMIDBServer(%s%d): %s", PAXOSType, serverID, exp.getMessage());
            exp.printStackTrace();
        }
    }

    public int getServerID() {
        return serverID;
    }

    public Registry getRegistry() {
        return registry;
    }

    public ReentrantLock getLock() {return paxosLock;}

    public Acceptor getAcceptor() { return acceptor; };

    // if the proposal acceptance received is a newly accepted one
    public boolean isNewAcceptance(Proposal proposal) {
        String pKey = proposal.getReqBody().getKey();
        return !proposalLastAccepted.containsKey(pKey) || proposalLastAccepted.get(pKey) < proposal.getProposalID();
    }

    // if the proposal acceptance received matches any previous proposal received
    public boolean isCurrentAcceptance(Proposal proposal) {
        return proposalLastAccepted.get(proposal.getReqBody().getKey()) == proposal.getProposalID();
    }

    // add a newly accepted proposal to record
    public void addNewAcceptance(Proposal proposal) {
        String pKey = proposal.getReqBody().getKey();
        proposalLastAccepted.put(pKey, proposal.getProposalID());
        if (proposalAcceptedCount.containsKey(pKey)) {
            proposalAcceptedCount.get(pKey).clear();
        } else {
            proposalAcceptedCount.put(pKey, new HashSet<>());
        }
    }

    // add the received proposal acceptance to counting record
    public void addCurrentAcceptance(Proposal proposal, int acceptorID) {
        proposalAcceptedCount.get(proposal.getReqBody().getKey()).add(acceptorID);
    }

    // get the number of acceptance received for the current proposal
    public int getAcceptanceNum(Proposal proposal) {
        return proposalAcceptedCount.get(proposal.getReqBody().getKey()).size();
    }

    // process the db operation the proposal contains, if the same proposal was processed before, use the response generated before directly
    public String processReq(Proposal proposal) throws IOException {
        // the key the proposal is editing
        String pKey = proposal.getReqBody().getKey();
        // proposal ID of the proposal
        long proposalID = proposal.getProposalID();
        // if the same proposal has been processed before, do not process again but return the existing response
        if (!proposalLastProcessed.containsKey(pKey)) {
            proposalLastProcessed.put(pKey, new HashMap<>());
        }
        if (!proposalLastProcessed.get(pKey).containsKey(proposalID)) {
            // if the proposal has not been processed before, store the result to cache
            proposalLastProcessed.get(pKey).put(proposalID, processReq(proposal.getReqBody()));
        }
        // return the stored result
        return proposalLastProcessed.get(pKey).get(proposalID);
    }

    @Override
    public String DBRequest(String req) throws RemoteException {
        String rsp = null;
        try {
            Log.Info("server %d receives from client(%s): %s", serverID, RemoteServer.getClientHost(), req);
            ReqBody reqBody = new ReqBody(req);
            if (reqBody.getAction() == ServerConfig.ACTION_GET) {
                // no PAXOS needed for GET operation
                rsp = processReq(reqBody);
            } else {
                // for PUT/DELETE operation, use PAXOS logic, ask proposer to prepare the request
                rsp = proposer.sendPrepare(req);
            }
        } catch ( ServerNotActiveException | IOException exp) {
            Log.Error("server error: %s", exp.getMessage());
            exp.printStackTrace();
            rsp = new RspBody(ServerConfig.SERVER_ERROR, exp.getMessage()).toJSONString();
        }
        Log.Info("server %d send to client: %s", serverID, rsp);
        return rsp;
    }
}
