package com.xforceplus.ultraman.oqsengine.storage.undo.command;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/21/2020 5:07 PM
 * 功能描述:
 * 修改历史:
 */
public class DefaultStorageCommandExecutor implements StorageCommandExecutor {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Map<TransactionResourceType, Map<OpType, StorageCommand>> storageCommands = new HashMap<>();

    public void register(TransactionResourceType transactionResourceType, OpType opType, StorageCommand cmd) {
        if (transactionResourceType == null || opType == null || cmd == null) {
            return;
        }
        if (!storageCommands.containsKey(transactionResourceType)) {
            storageCommands.put(transactionResourceType, new HashMap());
        }
        storageCommands.get(transactionResourceType).put(opType, cmd);
    }

    @Override
    public Object execute(TransactionResource res, OpType opType, Object data) throws SQLException {
        return selectCommand(res.type(), opType).execute(res, data);
    }

    @Override
    public Object executeUndo(TransactionResource res, OpType opType, Object data) throws SQLException {
        return ((UndoStorageCommand) selectCommand(res.type(), opType)).executeUndo(res, data);
    }

    private StorageCommand selectCommand(TransactionResourceType transactionResourceType, OpType opType) throws SQLException {
        if (transactionResourceType == null || opType == null
            || !storageCommands.containsKey(transactionResourceType)
            || storageCommands.get(transactionResourceType) == null
            || !storageCommands.get(transactionResourceType).containsKey(opType)
            || storageCommands.get(transactionResourceType).get(opType) == null
        ) {
            String errMsg = String.format("can't find storageCommand by dbType-%s,opType-%s", transactionResourceType, opType);
            logger.error(errMsg);
            throw new SQLException(errMsg);
        }
        return storageCommands.get(transactionResourceType).get(opType);
    }

}
