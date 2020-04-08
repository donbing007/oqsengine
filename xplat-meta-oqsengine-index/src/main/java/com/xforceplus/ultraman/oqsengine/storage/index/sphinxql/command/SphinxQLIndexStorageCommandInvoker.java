package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

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
 * 创建时间: 3/27/2020 5:01 PM
 * 功能描述:
 * 修改历史:
 */
public class SphinxQLIndexStorageCommandInvoker extends AbstractStorageCommandInvoker {

    final Logger logger = LoggerFactory.getLogger(SphinxQLIndexStorageCommandInvoker.class);

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    public SphinxQLIndexStorageCommandInvoker(String indexTableName){
        storageCommands = new HashMap<>();
        storageCommands.put(OpTypeEnum.BUILD, new BuildStorageCommand(storageStrategyFactory, indexTableName));
        storageCommands.put(OpTypeEnum.REPLACE, new ReplaceStorageCommand(storageStrategyFactory, indexTableName));
        storageCommands.put(OpTypeEnum.REPLACE_ATTRIBUTE, new ReplaceAttributeStorageCommand(storageStrategyFactory, indexTableName));
        storageCommands.put(OpTypeEnum.DELETE, new DeleteStorageCommand(indexTableName));
    }

    @Override
    public Object execute(OpTypeEnum opType, Connection conn, Object data) throws SQLException {
        return selectCommand(opType).execute(conn, data);
    }

}
