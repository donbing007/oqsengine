package com.xforceplus.ultraman.oqsengine.storage.undo;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/25/2020 4:38 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoExecutor {

    private BlockingQueue<UndoLog> undoLogQ;
    private StorageCommand cmd;
    private Object undoData;

    public UndoExecutor(StorageCommand cmd, Object undoData) {
        this.cmd = cmd;
        this.undoData = undoData;
    }

    public void execute(TransactionResource resource) {
        try {
            cmd.execute(resource, undoData);
        } catch (SQLException e) {
            if(undoLogQ != null) {
                undoLogQ.add(
                        new UndoLog(resource.dbType().name(), cmd.opType().name(), undoData)
                );
            }
        }
    }

    public void setErrorTransactionResourceQ(BlockingQueue<UndoLog> undoLogQ) {
        this.undoLogQ = undoLogQ;
    }
}
