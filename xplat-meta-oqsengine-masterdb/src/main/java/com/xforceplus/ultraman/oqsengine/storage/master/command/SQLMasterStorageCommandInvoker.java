package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:18 PM
 * 功能描述:
 * 修改历史:
 */
public class SQLMasterStorageCommandInvoker implements StorageCommandInvoker {

    final Logger logger = LoggerFactory.getLogger(SQLMasterStorageCommandInvoker.class);


    @Resource(name = "tableNameSelector")
    private Selector<String> tableNameSelector;

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    private Map<OpTypeEnum, StorageCommand> storageCommands;

    public SQLMasterStorageCommandInvoker(){
        storageCommands = new HashMap<>();
        storageCommands.put(OpTypeEnum.BUILD, new BuildStorageCommand(storageStrategyFactory, tableNameSelector));
        storageCommands.put(OpTypeEnum.REPLACE, new ReplaceStorageCommand(storageStrategyFactory, tableNameSelector));
        storageCommands.put(OpTypeEnum.DELETE, new DeleteStorageCommand(tableNameSelector));
    }

    @Override
    public Object build(TransactionResource resource, Object data) throws SQLException {
        return selectCommand(OpTypeEnum.BUILD).execute(resource, data);
    }

    @Override
    public Object replace(TransactionResource resource, Object data) throws SQLException {
        return selectCommand(OpTypeEnum.REPLACE).execute(resource, data);
    }

    @Override
    public Object delete(TransactionResource resource, Object data) throws SQLException {
        return selectCommand(OpTypeEnum.DELETE).execute(resource, data);
    }

    @Override
    public StorageCommand selectCommand(OpTypeEnum typeEnum) {
        if(!storageCommands.containsKey(typeEnum) || storageCommands.get(typeEnum) == null) {
            String message = String.format("can't find storageCommand %s", typeEnum.name());
            logger.error(message);
            return null;
        }
        return storageCommands.get(typeEnum);
    }
}
