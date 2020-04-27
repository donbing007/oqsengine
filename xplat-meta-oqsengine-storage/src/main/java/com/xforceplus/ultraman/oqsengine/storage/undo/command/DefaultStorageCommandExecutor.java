package com.xforceplus.ultraman.oqsengine.storage.undo.command;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbType;
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

    protected Map<DbType, Map<OpType, StorageCommand>> storageCommands = new HashMap<>();

    public void register(DbType dbType, OpType opType, StorageCommand cmd){
        if(dbType == null || opType == null || cmd == null) {
            return;
        }
        if(!storageCommands.containsKey(dbType)) {
            storageCommands.put(dbType, new HashMap());
        }
        storageCommands.get(dbType).put(opType, cmd);
    }

    @Override
    public Object execute(TransactionResource res, OpType opType, Object data) throws SQLException {
        return selectCommand(res.dbType(), opType).execute(res, data);
    }

    @Override
    public Object executeUndo(TransactionResource res, OpType opType, Object data) throws SQLException {
        return ((UndoStorageCommand)selectCommand(res.dbType(), opType)).executeUndo(res, data);
    }

    private StorageCommand selectCommand(DbType dbType, OpType opType) throws SQLException{
        if(dbType == null || opType == null
                || !storageCommands.containsKey(dbType)
                || storageCommands.get(dbType) == null
                || !storageCommands.get(dbType).containsKey(opType)
                || storageCommands.get(dbType).get(opType) == null
        ) {
            String errMsg = String.format("can't find storageCommand by dbType-%s,opType-%s", dbType, opType);
            logger.error(errMsg);
            throw new SQLException(errMsg);
        }
        return storageCommands.get(dbType).get(opType);
    }

}
