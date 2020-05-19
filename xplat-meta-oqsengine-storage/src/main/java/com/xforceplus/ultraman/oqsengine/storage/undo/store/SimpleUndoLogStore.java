package com.xforceplus.ultraman.oqsengine.storage.undo.store;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.UndoLogStatus;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/22/2020 4:47 PM
 * 功能描述:
 * 修改历史:
 */
public class SimpleUndoLogStore implements UndoLogStore {

    private ConcurrentMap<Long, Map<String, UndoLog>> store = new ConcurrentHashMap<>();

    private ConcurrentLinkedQueue<UndoLog> queue = new ConcurrentLinkedQueue<>();

    @Override
    public UndoLog get(Long txId, TransactionResourceType transactionResourceType, String shardKey) {
        return store.containsKey(txId) ? store.get(txId).get(dbKey(transactionResourceType, shardKey)) : null;
    }

    @Override
    public synchronized boolean save(Long txId, TransactionResourceType transactionResourceType, String shardKey, UndoLog undoLog) {
        undoLog.setTxId(txId);
        undoLog.setTransactionResourceType(transactionResourceType);
        undoLog.setShardKey(shardKey);
        undoLog.setTime(System.currentTimeMillis());

        store.putIfAbsent(txId, new HashMap<>());
        store.get(txId).put(dbKey(transactionResourceType, shardKey), undoLog);

        return true;
    }

    @Override
    public boolean isExist(Long txId) {
        return store.containsKey(txId);
    }

    @Override
    public synchronized boolean tryRemove(Long txId) {
        if (isExist(txId)) {
            Map txMap = store.get(txId);
            if (txMap.isEmpty()) {
                store.remove(txId);
            }
        }
        return true;
    }

    @Override
    public synchronized boolean remove(Long txId, TransactionResourceType transactionResourceType, String shardKey) {
        if (isExist(txId)) {
            ((Map) store.get(txId)).remove(dbKey(transactionResourceType, shardKey));
        }
        return true;
    }

    @Override
    public synchronized boolean removeItem(Long txId, TransactionResourceType transactionResourceType, String shardKey, int index) {
        if (isExist(txId) && store.get(txId).containsKey(dbKey(transactionResourceType, shardKey))) {
            Map<String, UndoLog> txMap = store.get(txId);
            UndoLog undoLog = txMap.get(dbKey(transactionResourceType, shardKey));
            undoLog.getItems().remove(index);
            txMap.put(dbKey(transactionResourceType, shardKey), undoLog);
            store.put(txId, txMap);
        }
        return true;
    }

    @Override
    public synchronized boolean updateStatus(Long txId, TransactionResourceType transactionResourceType, String shardKey, UndoLogStatus status) {
        if (isExist(txId) && store.get(txId).containsKey(dbKey(transactionResourceType, shardKey))) {
            Map<String, UndoLog> txMap = store.get(txId);
            UndoLog undoLog = txMap.get(dbKey(transactionResourceType, shardKey));
            undoLog.setStatus(status.value());
            txMap.put(dbKey(transactionResourceType, shardKey), undoLog);
            store.put(txId, txMap);
        }
        return true;
    }

    @Override
    public synchronized Queue<UndoLog> getUndoLogQueue(List<Integer> statuss) {
        if (queue.isEmpty()) {
            store.values().stream()
                    .filter(Objects::nonNull)
                    .flatMap(v -> v.values().stream())
                    .sorted(Comparator.comparingLong(UndoLog::getTime))
                    .forEach(undoLog -> {
                        if (statuss.contains(undoLog.getStatus())) {
                            queue.add(undoLog);
                        }
                    });
        }
        return queue;
    }

    String dbKey(TransactionResourceType transactionResourceType, String shardKey) {
        return transactionResourceType.name() + "-" + shardKey;
    }

}
