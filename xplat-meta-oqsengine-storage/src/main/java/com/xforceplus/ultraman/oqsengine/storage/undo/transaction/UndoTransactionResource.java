package com.xforceplus.ultraman.oqsengine.storage.undo.transaction;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpType;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLogItem;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建时间: 4/8/2020 11:00 AM
 * 功能描述:
 * 修改历史:
 * @author youyifan
 * @param <V>
 */
public abstract class UndoTransactionResource<V> implements TransactionResource<V> {

    protected boolean committed;

    protected UndoLog undoLog = new UndoLog();

    public UndoTransactionResource() {
        undoLog.setDbType(this.dbType());
    }

    public void addUndoLogItem(OpType opType, Object obj) {
        UndoLogItem undoLogItem = new UndoLogItem(opType, obj);
        this.undoLog.getItems().add(undoLogItem);
    }

    public UndoLog undoLog() {
        return undoLog;
    }

    public void committed() {
        committed = true;
    }

    public boolean isCommitted() {
        return this.committed;
    }

    public void setUndoLog(UndoLog undoLog) {
        this.undoLog = undoLog;
    }
}
