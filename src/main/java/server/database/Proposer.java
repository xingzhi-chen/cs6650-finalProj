package server.database;

import server.database.config.DBConfig;
import config.Log;
import server.config.DBReq;
import server.config.DBRsp;
import server.config.ServerConfig;
import server.database.config.PAXOSPrepareResult;
import server.database.config.Proposal;
import server.database.config.RMIAcceptorInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;

public class Proposer {
    // milliseconds per year
    private final long MILLISEC_PER_YEAR = 86400 * 365 * 1000;
    // the timestamp used for the last proposal
    private long lastTimestamp = 0;
    // the server object the proposer is on
    private final DBServer myServer;

    // generate a globally unique increasing proposal ID, format: {timestamp in this year} + 0 + {proposer ID}
    private long generateProposalID() {
        // use the timestamp as it is always increasing, if two requests get in at the same millisecond, add 1 to the second request to distinguish
        long timestamp = Math.max(new Date().getTime() % MILLISEC_PER_YEAR, lastTimestamp + 1);
        lastTimestamp = timestamp;
        // use the last 2 digits of the proposal ID to distinguish among proposers
        return timestamp * 100 + myServer.getServerID();
    }

    public Proposer(DBServer myServer) {
        // bind the proposer to its server
        this.myServer = myServer;
    }

    public synchronized String sendPrepare(String req) {
        DBReq reqBody = new DBReq(req);
        // transfer client request to proposal
        Proposal proposal = new Proposal(generateProposalID(), reqBody);
        // set the value to be proposed
        Proposal proposedVal = proposal;
        Log.Info("Proposer %d send prepare for request %s, generate proposalID: %d", myServer.getServerID(), req, proposal.getProposalID());
        // the number of acceptors that promised / denied the proposal
        int cnt = 0, deniedCnt = 0;
        // send prepare request to all acceptors
        for(int i = 1; i <= DBConfig.ACCEPTOR_SIZE; i++) {
            try {
                RMIAcceptorInterface acceptor = (RMIAcceptorInterface) myServer.getRegistry().lookup(DBConfig.RPC_ACCEPTOR_NAME + i);
                String resStr = acceptor.PAXOSPrepare(proposal.toJSONString());
                PAXOSPrepareResult res = new PAXOSPrepareResult(resStr);
                if (res.getResCode() == DBConfig.PROMISED) {
                    // the acceptor promised
                    cnt++;
                    // check to get the latest proposal from the acceptor
                    if (res.getAcceptedID() > proposedVal.getProposalID()) {
                        proposedVal = res.getAcceptedVal();
                    }
                } else {
                    // the acceptor denied, record it for log purpose, distinguish it from network error
                    deniedCnt++;
                }
            } catch (NotBoundException | RemoteException exp) {
                // network error
                Log.Warn("fail to connect acceptor %d: %s", i, exp.getMessage());
            }
        }

        if (cnt >= DBConfig.QUORUM) {
            // majority consensus reached, send accept to all acceptors
            Log.Info("consensus reached for preparing request %s at Proposer %d", req, myServer.getServerID());
            return sendAccept(proposedVal);
        } else if (deniedCnt >= DBConfig.QUORUM) {
            // the proposal is not the latest and majority acceptor deny it
            Log.Error("Proposer %d does not get majority consensus when preparing %s", myServer.getServerID(), req);
            return new DBRsp(ServerConfig.REQ_OUTDATED, ServerConfig.errorMsg.get(ServerConfig.REQ_OUTDATED)).toJSONString();
        } else {
            // the majority of acceptors suffer from network errors
            Log.Error("Proposer %d does not get majority consensus when preparing %s", myServer.getServerID(), req);
            return new DBRsp(ServerConfig.SERVER_ERROR, "Proposer does not get majority consensus").toJSONString();
        }
    }

    public String sendAccept(Proposal proposal) {
        // the number of acceptors that accepted / denied the proposal
        int cnt = 0, deniedCnt = 0;
        // send accept command to all acceptors
        for(int i = 1; i <= DBConfig.ACCEPTOR_SIZE; i++) {
            try {
                RMIAcceptorInterface acceptor = (RMIAcceptorInterface) myServer.getRegistry().lookup(DBConfig.RPC_ACCEPTOR_NAME + i);
                if (acceptor.PAXOSAccept(proposal.toJSONString()) == DBConfig.ACCEPTED) {
                    // the acceptor accept the proposal
                    cnt++;
                } else {
                    // some newer proposal was received by the acceptor, the acceptor denies the proposal
                    deniedCnt++;
                }
            } catch (NotBoundException | RemoteException exp) {
                // network error
                Log.Warn("fail to connect acceptor %d: %s", i, exp.getMessage());
            }
        }

        String rsp = null;
        try {
            myServer.getLock().lock();
            if (cnt >= DBConfig.QUORUM) {
                // majority consensus reached, process the request at local database and add it to the acceptance record (to share with the learner)
                Log.Info("consensus reached for accepting request at Proposer %d", myServer.getServerID());
                myServer.addNewAcceptance(proposal);
                rsp = myServer.processReq(proposal);
            } else if (deniedCnt >= DBConfig.QUORUM) {
                // the proposal is not the latest and majority acceptor deny it
                Log.Error("Proposer %d does not get majority consensus", myServer.getServerID());
                rsp = new DBRsp(ServerConfig.REQ_OUTDATED, ServerConfig.errorMsg.get(ServerConfig.REQ_OUTDATED)).toJSONString();
            } else {
                // the majority of acceptors suffer from network errors
                Log.Error("Proposer %d does not get majority consensus", myServer.getServerID());
                rsp = new DBRsp(ServerConfig.SERVER_ERROR, "Proposer does not get majority consensus").toJSONString();
            }
        } catch (IOException e) {
            // actually not gonna happen
        } finally {
            myServer.getLock().unlock();
        }
        return rsp;
    }

    public static void main(String[] args) {
        if (args.length < 1 || args[0].length() == 0) {
            Log.Error("command line format: java paxos.Acceptor {port} {serverID}");
            System.exit(0);
        }

        DBServer server = new DBServer(Integer.parseInt(args[0]), DBConfig.RPC_DB_NAME);
    }
}
