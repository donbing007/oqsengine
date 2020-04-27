package com.xforceplus.ultraman.oqsengine.storage.undo.store;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbType;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpType;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.UndoLogStatus;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLogItem;
import com.xforceplus.ultraman.oqsengine.storage.undo.util.CompressUtil;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 2:06 PM
 * 功能描述:
 * 修改历史:
 */
public class RedisUndoLogStore implements UndoLogStore {

    private static final String UNDO_LOG = "OQSENGINE_UNDO_LOG";

    private static final String UNDO_LOG_Q = "OQSENGINE_UNDO_LOG_Q";

    private RedissonClient redissonClient;

    public RedisUndoLogStore(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public UndoLog get(Long txId, DbType dbType, String shardKey) {
        return getUndoLog(txId, dbType, shardKey);
    }

    @Override
    public boolean save(Long txId, DbType dbType, String shardKey, UndoLog undoLog) {
        undoLog.setTxId(txId);
        undoLog.setDbType(dbType);
        undoLog.setShardKey(shardKey);
        undoLog.setTime(System.currentTimeMillis());
        return saveUndoLog(txId, dbType, shardKey, undoLog);
    }

    @Override
    public boolean isExist(Long txId) {
        return getUndoLog().containsKey(txId);
    }

    @Override
    public boolean remove(Long txId, DbType dbType, String shardKey) {
        Map<String, byte[]> txUndoMap = (Map) getUndoLog().get(txId);
        if (txUndoMap != null) {
            txUndoMap.remove(dbKey(dbType, shardKey));
        }

        if(txUndoMap.isEmpty()) {
            getUndoLog().remove(txId);
            return true;
        } else {
            return lockPut(txId, dbType, shardKey, txUndoMap);
        }
    }

    @Override
    public boolean removeItem(Long txId, DbType dbType, String shardKey, int index) {
        Map<String, byte[]> txUndoMap = (Map) getUndoLog().get(txId);
        UndoLog undoLog = (UndoLog) CompressUtil.decompressToObj(txUndoMap.get(dbKey(dbType, shardKey)));
        undoLog.getItems().set(index, null);

        txUndoMap.put(dbKey(dbType, shardKey), CompressUtil.compress(undoLog));

        return lockPut(txId, dbType, shardKey, txUndoMap);
    }

    @Override
    public boolean tryRemove(Long txId) {
        if (isExist(txId)) {
            RLock lock = redissonClient.getLock(txId + "");
            if (lock != null) {
                try {
                    lock.tryLock(10, 10, TimeUnit.SECONDS);

                    Map<String, byte[]> txUndoMap = (Map<String, byte[]>) getUndoLog().get(txId);

                    if(txUndoMap.isEmpty()) {
                        getUndoLog().remove(txId);
                    }

                    return true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public Queue<UndoLog> getUndoLogQueue(List<Integer> statuss) {
        List<UndoLog> undoLogs = new ArrayList<>();

        getUndoLog().readAllValues().stream()
                .filter(Objects::nonNull)
                .flatMap(obj -> ((Map)obj).values().stream())
                .sorted(Comparator.comparingLong(UndoLog::getTime))
                .forEach(v -> {
                    UndoLog undoLog = (UndoLog) CompressUtil.decompressToObj((byte[])v);
                    if(statuss != null && statuss.contains(undoLog.getStatus())) {
                        undoLogs.add(undoLog);
                    }
        });

        RQueue<UndoLog> queue = redissonClient.getQueue(UNDO_LOG_Q);
        queue.addAll(undoLogs);

        return queue;
    }

    @Override
    public boolean updateStatus(Long txId, DbType dbType, String shardKey, UndoLogStatus status) {
        UndoLog undoLog = getUndoLog(txId, dbType, shardKey);
        if(undoLog == null) { return false; }

        undoLog.setStatus(status.value());

        return saveUndoLog(txId, dbType, shardKey, undoLog);

    }

    RMap getUndoLog() {
        return redissonClient.getMap(UNDO_LOG);
    }

    UndoLog getUndoLog(Long txId, DbType dbType, String shardKey) {
        getUndoLog().putIfAbsent(txId, new HashMap<>());

        Map<String, byte[]> txUndoMap = (Map<String, byte[]>) getUndoLog().get(txId);

        return txUndoMap.containsKey(dbKey(dbType, shardKey)) ?
                (UndoLog) CompressUtil.decompressToObj(txUndoMap.get(dbKey(dbType, shardKey))) : null;
    }

    boolean saveUndoLog(Long txId, DbType dbType, String shardKey, UndoLog undoLog) {
        getUndoLog().putIfAbsent(txId, new HashMap<>());

        Map<String, byte[]> txUndoMap = (Map<String, byte[]>) getUndoLog().get(txId);

        txUndoMap.put(dbKey(dbType, shardKey), CompressUtil.compress(undoLog));

        return lockPut(txId, dbType, shardKey, txUndoMap);
    }

    boolean lockPut(Long txId, DbType dbType, String shardKey, Map<String, byte[]> txUndoMap) {
        RLock lock = redissonClient.getLock(lockKey(txId, dbType, shardKey));
        if (lock != null) {
            try {
                lock.tryLock(10, 10, TimeUnit.SECONDS);

                getUndoLog().put(txId, txUndoMap);

                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    String dbKey(DbType dbType, String shardKey) {
        return dbType.name() + "-" + shardKey;
    }

    String lockKey(Long txId, DbType dbType, String shardKey) {
        return "undolog-" + txId + "-" + dbType.name() + "-" + shardKey;
    }
}
