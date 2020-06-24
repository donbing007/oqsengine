package com.xforceplus.ultraman.oqsengine.storage.undo.command;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpType;
import com.xforceplus.ultraman.oqsengine.storage.undo.transaction.UndoTransactionResource;

import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建时间: 4/9/2020 2:00 PM
 * 功能描述:
 * 修改历史:
 * @author youyifan
 * @param <T>
 */
public abstract class UndoStorageCommand<T,R> implements StorageCommand<T,R> {

    protected void prepareUndoLog(TransactionResource resource, OpType opType, T data) {
        UndoTransactionResource undoResource = (UndoTransactionResource) resource;
        if (!undoResource.isCommitted()) {
            undoResource.addUndoLogItem(opType, data);
        }
    }

    /**
     * @param resource
     * @param data
     * @return
     * @throws SQLException
     */
    public abstract R executeUndo(TransactionResource resource, T data) throws SQLException;
}
