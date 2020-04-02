package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.AbstractStorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
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

    @Resource(name = "tableNameSelector")
    private Selector<String> tableNameSelector;

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    public SQLMasterStorageCommandInvoker(){
        storageCommands = new HashMap<>();
        storageCommands.put(OpTypeEnum.BUILD, new BuildStorageCommand(storageStrategyFactory, tableNameSelector));
        storageCommands.put(OpTypeEnum.REPLACE, new ReplaceStorageCommand(storageStrategyFactory, tableNameSelector));
        storageCommands.put(OpTypeEnum.DELETE, new DeleteStorageCommand(tableNameSelector));
    }

    @Override
    public Object execute(OpTypeEnum opType, Connection conn, Object data) throws SQLException {
        return selectCommand(opType).execute(conn, data);
    }

}
