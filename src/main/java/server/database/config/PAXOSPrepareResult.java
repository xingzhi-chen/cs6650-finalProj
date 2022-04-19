package server.database.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;
//import org.json.JSONObject;

// Data structure for acceptor preparation result
public class PAXOSPrepareResult {
    // values used for serialization for RMI
    private final String PROPOSAL_ID = "acceptedID";
    private final String ACCEPTED_VAL = "acceptedVal";
    private final String RES_CODE = "resCode";

    // proposal ID of the latest proposal the acceptor accepted
    private long acceptedID;
    // proposal value of the latest proposal the acceptor accepted
    private Proposal acceptedVal;
    // result code of this preparation process
    private int resCode;
//    private final GsonBuilder builder = new GsonBuilder();

    public PAXOSPrepareResult(long acceptedID, Proposal acceptedVal, int resCode) {
        this.acceptedID = acceptedID;
        this.acceptedVal = acceptedVal;
        this.resCode = resCode;
    }

    // parse result from JSON string
    public PAXOSPrepareResult(String resJSONStr) {
//        builder.serializeNulls();
//        Gson gson = builder.create();
//        PAXOSPrepareResult result = gson.fromJson(resJSONStr, PAXOSPrepareResult.class);
//        this.acceptedID = result.getAcceptedID();
//        this.acceptedVal = result.getAcceptedVal();
//        this.resCode = result.getResCode();
        JSONObject obj = new JSONObject(resJSONStr);
        this.acceptedID = obj.getLong(PROPOSAL_ID);
        if (obj.has(ACCEPTED_VAL)) {
            this.acceptedVal = new Proposal(obj.getString(ACCEPTED_VAL));
        } else {
            this.acceptedVal = null;
        }
        this.resCode = obj.getInt(RES_CODE);
    }

    // serialize result into JSON string for RMI communication
    public String toJSONString() {
//        builder.serializeNulls();
//        Gson gson = builder.create();
//        return gson.toJson(this);
        JSONObject obj = new JSONObject();
        obj.put(PROPOSAL_ID, acceptedID);
        if (acceptedVal != null) {
            obj.put(ACCEPTED_VAL, acceptedVal.toJSONString());
        }
        obj.put(RES_CODE, resCode);

        return obj.toString();
    }

    public long getAcceptedID() {
        return acceptedID;
    }

    public Proposal getAcceptedVal() {
        return acceptedVal;
    }

    public int getResCode() {
        return resCode;
    }
}
