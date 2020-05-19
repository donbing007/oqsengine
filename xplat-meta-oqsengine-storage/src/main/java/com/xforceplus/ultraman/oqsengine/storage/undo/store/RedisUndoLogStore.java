package com.xforceplus.ultraman.oqsengine.storage.undo.store;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.UndoLogStatus;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import com.xforceplus.ultraman.oqsengine.storage.undo.util.CompressUtil;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;

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
    public UndoLog get(Long txId, TransactionResourceType transactionResourceType, String shardKey) {
        return getUndoLog(txId, transactionResourceType, shardKey);
    }

    @Override
    public boolean save(Long txId, TransactionResourceType transactionResourceType, String shardKey, UndoLog undoLog) {
        undoLog.setTxId(txId);
        undoLog.setTransactionResourceType(transactionResourceType);
        undoLog.setShardKey(shardKey);
        undoLog.setTime(System.currentTimeMillis());
        return saveUndoLog(txId, transactionResourceType, shardKey, undoLog);
    }

    @Override
    public boolean isExist(Long txId) {
        return getUndoLog().containsKey(txId);
    }

    @Override
    public boolean remove(Long txId, TransactionResourceType transactionResourceType, String shardKey) {
        Map<String, byte[]> txUndoMap = (Map) getUndoLog().get(txId);
        if (txUndoMap != null) {
            txUndoMap.remove(dbKey(transactionResourceType, shardKey));
        }

        if (txUndoMap.isEmpty()) {
            getUndoLog().remove(txId);
            return true;
        } else {
            return lockPut(txId, transactionResourceType, shardKey, txUndoMap);
        }
    }

    @Override
    public boolean removeItem(Long txId, TransactionResourceType transactionResourceType, String shardKey, int index) {
        Map<String, byte[]> txUndoMap = (Map) getUndoLog().get(txId);
        UndoLog undoLog = (UndoLog) CompressUtil.decompressToObj(txUndoMap.get(dbKey(transactionResourceType, shardKey)));
        undoLog.getItems().set(index, null);

        txUndoMap.put(dbKey(transactionResourceType, shardKey), CompressUtil.compress(undoLog));

        return lockPut(txId, transactionResourceType, shardKey, txUndoMap);
    }

    @Override
    public boolean tryRemove(Long txId) {
        if (isExist(txId)) {
            RLock lock = redissonClient.getLock(txId + "");
            if (lock != null) {
                try {
                    lock.tryLock(10, 10, TimeUnit.SECONDS);

                    Map<String, byte[]> txUndoMap = (Map<String, byte[]>) getUndoLog().get(txId);

                    if (txUndoMap.isEmpty()) {
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
                .flatMap(obj -> ((Map) obj).values().stream())
                .sorted(Comparator.comparingLong(UndoLog::getTime))
                .forEach(v -> {
                    UndoLog undoLog = (UndoLog) CompressUtil.decompressToObj((byte[]) v);
                    if (statuss != null && statuss.contains(undoLog.getStatus())) {
                        undoLogs.add(undoLog);
                    }
                });

        RQueue<UndoLog> queue = redissonClient.getQueue(UNDO_LOG_Q);
        queue.addAll(undoLogs);

        return queue;
    }

    @Override
    public boolean updateStatus(Long txId, TransactionResourceType transactionResourceType, String shardKey, UndoLogStatus status) {
        UndoLog undoLog = getUndoLog(txId, transactionResourceType, shardKey);
        if (undoLog == null) {
            return false;
        }

        undoLog.setStatus(status.value());

        return saveUndoLog(txId, transactionResourceType, shardKey, undoLog);

    }

    RMap getUndoLog() {
        return redissonClient.getMap(UNDO_LOG);
    }

    UndoLog getUndoLog(Long txId, TransactionResourceType transactionResourceType, String shardKey) {
        getUndoLog().putIfAbsent(txId, new HashMap<>());

        Map<String, byte[]> txUndoMap = (Map<String, byte[]>) getUndoLog().get(txId);

        return txUndoMap.containsKey(dbKey(transactionResourceType, shardKey)) ?
                (UndoLog) CompressUtil.decompressToObj(txUndoMap.get(dbKey(transactionResourceType, shardKey))) : null;
    }

    boolean saveUndoLog(Long txId, TransactionResourceType transactionResourceType, String shardKey, UndoLog undoLog) {
        getUndoLog().putIfAbsent(txId, new HashMap<>());

        Map<String, byte[]> txUndoMap = (Map<String, byte[]>) getUndoLog().get(txId);

        txUndoMap.put(dbKey(transactionResourceType, shardKey), CompressUtil.compress(undoLog));

        return lockPut(txId, transactionResourceType, shardKey, txUndoMap);
    }

    boolean lockPut(Long txId, TransactionResourceType transactionResourceType, String shardKey, Map<String, byte[]> txUndoMap) {
        RLock lock = redissonClient.getLock(lockKey(txId, transactionResourceType, shardKey));
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

    String dbKey(TransactionResourceType transactionResourceType, String shardKey) {
        return transactionResourceType.name() + "-" + shardKey;
    }

    String lockKey(Long txId, TransactionResourceType transactionResourceType, String shardKey) {
        return "undolog-" + txId + "-" + transactionResourceType.name() + "-" + shardKey;
    }
}
