package server.database;


import org.junit.Test;
import server.config.DBReq;
import server.config.DBRsp;
import server.config.ServerConfig;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class DBServerTestClient {
    private DBInterface db;

    @org.junit.Before
    public void setUp() throws Exception {
        String host = "127.0.0.1";
        Registry registry = LocateRegistry.getRegistry(host);
        int serverID = 1;
        db = (DBInterface) registry.lookup(ServerConfig.RPC_DB_NAME + serverID);
    }

    @Test
    public void testPutNoAppend() throws RemoteException {
        String key = "key1";
        String value = "value1";
        String req = new DBReq(key, value, ServerConfig.ACTION_GET).toJSONString();
        DBRsp rspBody = new DBRsp(db.DBRequest(req));
        assert(rspBody.getResCode() == ServerConfig.ERROR_NO_EXIST);

        req = new DBReq(key, value, ServerConfig.ACTION_PUT).toJSONString();
        rspBody = new DBRsp(db.DBRequest(req));
        assert(rspBody.getResCode() == ServerConfig.SUCCESS);

        req = new DBReq(key, ServerConfig.ACTION_GET).toJSONString();
        rspBody = new DBRsp(db.DBRequest(req));
        assert(rspBody.getValue().get(0).equals(value));
    }

    @Test
    public void testPutAppend() throws RemoteException {
        String key = "key";
        String value1 = "value1";
        String value2 = "value2";

        String req = new DBReq(key, value1, ServerConfig.ACTION_PUT).toJSONString();
        db.DBRequest(req);
        req = new DBReq(key, value2, ServerConfig.ACTION_PUT, true).toJSONString();
        db.DBRequest(req);

        req = new DBReq(key, ServerConfig.ACTION_GET).toJSONString();
        DBRsp rspBody = new DBRsp(db.DBRequest(req));
        ArrayList<String> expValue = new ArrayList<>() {{
            add(value1);
            add(value2);
        }};
        assertEquals(rspBody.getValue(), expValue);
    }

    @Test
    public void testPutTimeout() throws RemoteException, InterruptedException {
        String key = "key";
        String value1 = "value1";
        String value2 = "value2";

        String req = new DBReq(key, value1, ServerConfig.ACTION_PUT, true, 1).toJSONString();
        db.DBRequest(req);
        Thread.sleep(1500);

        req = new DBReq(key, value2, ServerConfig.ACTION_PUT, true).toJSONString();
        db.DBRequest(req);

        req = new DBReq(key, ServerConfig.ACTION_GET).toJSONString();
        DBRsp rspBody = new DBRsp(db.DBRequest(req));
        ArrayList<String> expValue = new ArrayList<>() {{
            add(value2);
        }};
        assertEquals(rspBody.getValue(), expValue);
    }

    @Test
    public void testDelete() throws RemoteException {
        String key = "key1";
        String value = "value1";

        String req = new DBReq(key, value, ServerConfig.ACTION_PUT).toJSONString();
        DBRsp rspBody = new DBRsp(db.DBRequest(req));
        assert(rspBody.getResCode() == ServerConfig.SUCCESS);

        req = new DBReq(key, ServerConfig.ACTION_GET).toJSONString();
        rspBody = new DBRsp(db.DBRequest(req));
        assert(rspBody.getValue().get(0).equals(value));

        req = new DBReq(key, ServerConfig.ACTION_DELETE).toJSONString();
        rspBody = new DBRsp(db.DBRequest(req));
        assert(rspBody.getResCode() == ServerConfig.SUCCESS);

        req = new DBReq(key, ServerConfig.ACTION_GET).toJSONString();
        rspBody = new DBRsp(db.DBRequest(req));
        assert(rspBody.getResCode() == ServerConfig.ERROR_NO_EXIST);
    }
}