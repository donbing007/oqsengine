package com.xforceplus.ultraman.oqsengine.storage.undo.command;

import com.xforceplus.ultraman.oqsengine.storage.transaction.AbstractTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.util.UndoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 3:58 PM
 * 功能描述:
 * 修改历史:
 */
public abstract class AbstractStorageCommandInvoker implements StorageCommandInvoker {

    final Logger logger = LoggerFactory.getLogger(AbstractStorageCommandInvoker.class);

    protected Map<OpTypeEnum, StorageCommand> storageCommands;

    @Override
    public StorageCommand selectCommand(OpTypeEnum typeEnum) {
        if(!storageCommands.containsKey(typeEnum) || storageCommands.get(typeEnum) == null) {
            String message = String.format("can't find storageCommand %s", typeEnum.name());
            logger.error(message);
            return null;
        }
        return storageCommands.get(typeEnum);
    }

    @Override
    public Object execute(TransactionResource resource, OpTypeEnum opType, Object data) throws SQLException {
        ((AbstractTransactionResource)resource).addUndoInfo(UndoUtil.getShardKeyFromDbKey((String)resource.key()), opType, data);
        return selectCommand(opType).execute((Connection) resource.value(), data);
    }

}
