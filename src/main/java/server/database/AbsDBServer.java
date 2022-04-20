package server.database;

import server.config.DBReq;
import server.config.DBRsp;
import server.config.ServerConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
* base class for both TCP and UDP server, includes the database operations and request parsing process
 */
public abstract class AbsDBServer{
    private class Pair{
        public long timestamp;
        public String value;
        public Pair(long timestamp, String value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    // data storage
    protected HashMap<String, ArrayList<Pair>> database = new HashMap<>();
    // lock for multithreading operations, all DB operations should be thread safe
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public AbsDBServer() {}

    // perform the PUT action
    private DBRsp actionPut(String key, String value, boolean append, int timeout) {
        lock.writeLock().lock();
        try {
//            Log.Debug(String.format("start put %s", key));
            if (!database.containsKey(key)) {
                database.put(key, new ArrayList<>());
            }
            if (!append) {
                database.get(key).clear();
            }
            long timestamp = Long.MAX_VALUE;
            if (timeout > 0) {
                timestamp = new Date().getTime() + timeout * 1000;
            }
            database.get(key).add(new Pair(timestamp, value));
//            Log.Debug(String.format("end put %s", key));
        } finally {
            lock.writeLock().unlock();
            return new DBRsp(ServerConfig.SUCCESS, ServerConfig.errorMsg.get(ServerConfig.SUCCESS));
        }
    }

    // perform the GET operation, return error code if value does not exist
    private DBRsp actionGet(String key) {
        lock.readLock().lock();
        int resCode = ServerConfig.SUCCESS;
        ArrayList<String> val = null;
        try {
//            Log.Debug(String.format("start get %s", key));
            if (database.containsKey(key)) {
                long timestamp = new Date().getTime();
                ArrayList<Pair> outdated = new ArrayList<>();
                val = new ArrayList<>();
                for(Pair pair : database.get(key)) {
                    if (pair.timestamp >= timestamp) {
                        val.add(pair.value);
                    } else {
                        outdated.add(pair);
                    }
                }
                database.get(key).removeAll(outdated);
                if (val.isEmpty()) {
                    resCode = ServerConfig.ERROR_NO_EXIST;
                }
            } else {
                resCode = ServerConfig.ERROR_NO_EXIST;
            }
//            Log.Debug(String.format("end get %s", key));
        } finally {
            lock.readLock().unlock();
            return new DBRsp(resCode, val, ServerConfig.errorMsg.get(resCode));
        }
    }

    // perform the DELETE operation, return error code if value does not exist
    private DBRsp actionDelete(String key) {
        lock.writeLock().lock();
        int resCode = ServerConfig.SUCCESS;
        try {
//            Log.Debug(String.format("start delete %s", key));
            if (database.containsKey(key)) {
                database.remove(key);
            } else {
                resCode = ServerConfig.ERROR_NO_EXIST;
            }
//            Log.Debug(String.format("end delete %s", key));
        } finally {
            lock.writeLock().unlock();
            return new DBRsp(resCode, ServerConfig.errorMsg.get(resCode));
        }
    }

    // parse the request body, and forward the content to different operation functions
    protected String processReq(DBReq reqBody) throws IOException {
        DBRsp rsp = null;
        switch (reqBody.getAction()) {
            case ServerConfig.ACTION_PUT:
                rsp = actionPut(reqBody.getKey(), reqBody.getValue(), reqBody.getAppend(), reqBody.getTimeout());
                break;
            case ServerConfig.ACTION_GET:
                rsp = actionGet(reqBody.getKey());
                break;
            case ServerConfig.ACTION_DELETE:
                rsp = actionDelete(reqBody.getKey());
                break;
            default:
                rsp = new DBRsp(ServerConfig.ERROR_NO_ACTION, ServerConfig.errorMsg.get(ServerConfig.ERROR_NO_ACTION));
        }
        return rsp.toJSONString();
    }
}
