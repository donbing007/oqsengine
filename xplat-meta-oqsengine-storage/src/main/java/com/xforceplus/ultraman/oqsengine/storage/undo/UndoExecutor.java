package com.xforceplus.ultraman.oqsengine.storage.undo;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.UndoLogStatus;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLogItem;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.transaction.UndoTransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/25/2020 4:38 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoExecutor {

    final Logger logger = LoggerFactory.getLogger(UndoExecutor.class);

    private UndoLogStore undoLogStore;
    private StorageCommandExecutor storageCommandExecutor;
    private boolean mockError;

    public UndoExecutor(
            UndoLogStore undoLogStore,
            StorageCommandExecutor storageCommandExecutor) {
        this.undoLogStore = undoLogStore;
        if (undoLogStore == null) {
            logger.error("UndoExecutor set UndoLogStore to null");
        }
        this.storageCommandExecutor = storageCommandExecutor;
        this.mockError = false;
    }

    public void undo(TransactionResource resource) {
        UndoLog undoLog = getUndoLog(resource);

        logger.debug("start undo {} items", undoLog.getItems().size());
        boolean isComplete = true;
        for (int i = undoLog.getItems().size() - 1; i >= 0; i--) {
            UndoLogItem item = undoLog.getItems().get(i);

            try {
                storageCommandExecutor.executeUndo(resource, item.getOpType(), item.getData());
                resource.commit();
                removeUndoLogItem(i, undoLog);
            } catch (SQLException e) {
                logger.error("undo failed ", item.getOpType().name());
                isComplete = false;
            }
        }

        if (isComplete) {
            removeUndoLog(undoLog);
        }

        logger.debug("finish undo {} items");
    }

    public void saveUndoLog(Long txId, TransactionResource res) {
        UndoLog undoLog = getUndoLog(res);
        logger.debug("save undo infos {} items in store ", undoLog.getItems().size());
        this.undoLogStore.save(txId, undoLog.getDbType(), undoLog.getShardKey(), undoLog);
    }

    public void updateUndoLogStatus(Long txId, TransactionResource res, UndoLogStatus status) {
        UndoLog undoLog = getUndoLog(res);
        this.undoLogStore.updateStatus(txId, undoLog.getDbType(), undoLog.getShardKey(), status);
        logger.debug("[UndoExecutor UNDO] success to clear undo log in store");
    }

    public void mock() throws SQLException {
        if (mockError) {
            throw new SQLException("mock throws SQLException when commits finished");
        }
    }

    public void setMockError(boolean mockError) {
        this.mockError = mockError;
    }

    private void removeUndoLogItem(int index, UndoLog undoLog) {
        this.undoLogStore.removeItem(undoLog.getTxId(), undoLog.getDbType(), undoLog.getShardKey(), index);
        logger.debug("success to clear undo log in store");
    }

    private void removeUndoLog(UndoLog undoLog) {
        this.undoLogStore.remove(undoLog.getTxId(), undoLog.getDbType(), undoLog.getShardKey());
        this.undoLogStore.tryRemove(undoLog.getTxId());
        logger.debug("success to clear undo log in store");
    }

    private UndoLog getUndoLog(TransactionResource res) {
        return ((UndoTransactionResource) res).undoLog();
    }
}
