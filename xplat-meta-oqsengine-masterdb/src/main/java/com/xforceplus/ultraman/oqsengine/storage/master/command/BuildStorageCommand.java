package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 3:41 PM
 * 功能描述:
 * 修改历史:
 */
public class BuildStorageCommand implements StorageCommand {

    final Logger logger = LoggerFactory.getLogger(BuildStorageCommand.class);

    private StorageStrategyFactory storageStrategyFactory;

    private Selector<String> tableNameSelector;

    public BuildStorageCommand(StorageStrategyFactory storageStrategyFactory, Selector<String> tableNameSelector){
        this.storageStrategyFactory = storageStrategyFactory;
        this.tableNameSelector = tableNameSelector;
    }

    @Override
    public OpTypeEnum opType() {
        return OpTypeEnum.BUILD;
    }

    @Override
    public Object execute(TransactionResource resource, Object data) throws SQLException {
        return null;
    }

}
