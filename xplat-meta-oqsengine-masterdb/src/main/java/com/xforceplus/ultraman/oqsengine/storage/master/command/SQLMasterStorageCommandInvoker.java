package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.AbstractTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.AbstractStorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.util.UndoUtil;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:18 PM
 * 功能描述:
 * 修改历史:
 */
public class SQLMasterStorageCommandInvoker extends AbstractStorageCommandInvoker {

    final Logger logger = LoggerFactory.getLogger(SQLMasterStorageCommandInvoker.class);

    public SQLMasterStorageCommandInvoker(Selector<String> tableNameSelector, StorageStrategyFactory storageStrategyFactory){
        storageCommands = new HashMap<>();
        storageCommands.put(OpTypeEnum.BUILD, new BuildStorageCommand(storageStrategyFactory, tableNameSelector));
        storageCommands.put(OpTypeEnum.REPLACE, new ReplaceStorageCommand(storageStrategyFactory, tableNameSelector));
        storageCommands.put(OpTypeEnum.DELETE, new DeleteStorageCommand(tableNameSelector));
    }
    
}
