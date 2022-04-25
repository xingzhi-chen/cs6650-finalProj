package server.database.config;

//import org.json.JSONObject;
import org.json.JSONObject;
import server.config.DBReq;

public class Proposal {
    // values used for serialization
    private final String PROPOSAL_ID = "proposalID";
    private final String REQ_BODY = "reqBody";

    private final long proposalID;
    // values of the client request
    private final DBReq reqBody;

    public Proposal(long proposalID, DBReq reqBody) {
        this.proposalID = proposalID;
        this.reqBody = reqBody;
    }

    // parse proposal from JSON string
    public Proposal(String JSONStr) {
        JSONObject obj = new JSONObject(JSONStr);
        proposalID = obj.getLong(PROPOSAL_ID);
        reqBody = new DBReq(obj.getString(REQ_BODY));
    }

    // serialize the proposal to JSON string for RMI communication
    public String toJSONString() {
        JSONObject obj = new JSONObject();
        obj.put(PROPOSAL_ID, proposalID);
        obj.put(REQ_BODY, reqBody.toJSONString());
        return obj.toString();
    }

    public long getProposalID() {
        return proposalID;
    }

    public DBReq getReqBody() {
        return reqBody;
    }
}