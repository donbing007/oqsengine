package com.xforceplus.ultraman.oqsengine.storage.undo.store;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.UndoLogStatus;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;

import java.util.List;
import java.util.Queue;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 1:59 PM
 * 功能描述:
 * 修改历史:
 */
public interface UndoLogStore {

    UndoLog get(Long txId, TransactionResourceType transactionResourceType, String shardKey);

    boolean save(Long txId, TransactionResourceType transactionResourceType, String shardKey, UndoLog undoLog);

    boolean isExist(Long txId);

    boolean tryRemove(Long txId);

    boolean remove(Long txId, TransactionResourceType transactionResourceType, String shardKey);

    boolean removeItem(Long txId, TransactionResourceType transactionResourceType, String shardKey, int index);

    boolean updateStatus(Long txId, TransactionResourceType transactionResourceType, String shardKey, UndoLogStatus status);

    Queue<UndoLog> getUndoLogQueue(List<Integer> statuss);
}
