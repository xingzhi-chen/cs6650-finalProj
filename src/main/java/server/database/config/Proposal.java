package server.database.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import org.json.JSONObject;
import org.json.JSONObject;
import server.config.ReqBody;

public class Proposal {
    // values used for serialization
    private final String PROPOSAL_ID = "proposalID";
    private final String REQ_BODY = "reqBody";

    private final long proposalID;
    // values of the client request
    private final ReqBody reqBody;
//    private final GsonBuilder builder = new GsonBuilder();

    public Proposal(long proposalID, ReqBody reqBody) {
        this.proposalID = proposalID;
        this.reqBody = reqBody;
    }

    // parse proposal from JSON string
    public Proposal(String JSONStr) {
//        builder.serializeNulls();
//        Gson gson = builder.create();
//        Proposal proposal = gson.fromJson(JSONStr, Proposal.class);
//        this.proposalID = proposal.getProposalID();
//        this.reqBody = proposal.getReqBody();
        JSONObject obj = new JSONObject(JSONStr);
        proposalID = obj.getLong(PROPOSAL_ID);
        reqBody = new ReqBody(obj.getString(REQ_BODY));
    }

    // serialize the proposal to JSON string for RMI communication
    public String toJSONString() {
//        builder.serializeNulls();
//        Gson gson = builder.create();
//        return gson.toJson(this);
        JSONObject obj = new JSONObject();
        obj.put(PROPOSAL_ID, proposalID);
        obj.put(REQ_BODY, reqBody.toJSONString());
        return obj.toString();
    }

    public long getProposalID() {
        return proposalID;
    }

    public ReqBody getReqBody() {
        return reqBody;
    }
}