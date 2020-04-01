package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.master.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:09 PM
 * 功能描述:
 * 修改历史:
 */
public class ReplaceStorageCommand implements StorageCommand {

    final Logger logger = LoggerFactory.getLogger(ReplaceStorageCommand.class);

    private StorageStrategyFactory storageStrategyFactory;

    private Selector<String> tableNameSelector;

    public ReplaceStorageCommand(StorageStrategyFactory storageStrategyFactory, Selector<String> tableNameSelector){
        this.storageStrategyFactory = storageStrategyFactory;
        this.tableNameSelector = tableNameSelector;
    }

    @Override
    public OpTypeEnum opType() {
        return OpTypeEnum.REPLACE;
    }

    @Override
    public Object execute(TransactionResource resource, Object data) throws SQLException {
        return null;
    }

}
