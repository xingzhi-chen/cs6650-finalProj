package client.comm;

import com.google.gson.JsonObject;
import config.GlobalConfig;


public class RegisterHandler extends HttpHandler{

    ClientComm comm;

    public RegisterHandler (ClientComm comm) {
        super(comm);
    }

    @Override
    public void processSuccessRsp (JsonObject jsonObject) {
        this.comm.setUsername(jsonObject.get(GlobalConfig.USERNAME).getAsString());
    }

    @Override
    public boolean isValidResponse(JsonObject jsonObject) {
        return super.isValidResponse(jsonObject)
                && jsonObject.has(GlobalConfig.USERNAME);
    }
}
