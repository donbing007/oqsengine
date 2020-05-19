package com.xforceplus.ultraman.oqsengine.storage.undo.transaction;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpType;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.UndoLogStatus;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLogItem;

import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建时间: 4/8/2020 11:00 AM
 * 功能描述:
 * 修改历史:
 *
 * @param <V>
 * @author youyifan
 */
public abstract class UndoTransactionResource<V> implements TransactionResource<V> {

    protected boolean committed;

    protected UndoExecutor undoExecutor;

    protected UndoLog undoLog = new UndoLog();

    public UndoTransactionResource() {
        undoLog.setTransactionResourceType(this.type());
    }

    public void addUndoLogItem(OpType opType, Object obj) {
        UndoLogItem undoLogItem = new UndoLogItem(opType, obj);
        this.undoLog.getItems().add(undoLogItem);
    }

    public UndoLog undoLog() {
        return undoLog;
    }

    public boolean isCommitted() {
        return this.committed;
    }

    public void createUndoLog(Long txId) {
        if (undoExecutor != null) {
            undoLog.setTxId(txId);
            undoExecutor.saveUndoLog(undoLog);
        }
    }

    protected void saveCommitStatus() {
        if (undoExecutor != null) {
            committed = true;
            undoExecutor.updateUndoLogStatus(undoLog, UndoLogStatus.COMMITED);
        }
    }

    public void undo(boolean commit) throws SQLException {
        if (commit && committed) {
            if (undoExecutor != null) {
                undoExecutor.undo(this);
            }
        } else {
            rollback();
        }
    }

    public void setUndoLog(UndoLog undoLog) {
        this.undoLog = undoLog;
    }

    public void setUndoExecutor(UndoExecutor undoExecutor) {
        this.undoExecutor = undoExecutor;
    }
}
