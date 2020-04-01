package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:58 PM
 * 功能描述:
 * 修改历史:
 */
public class ReplaceStorageCommand implements StorageCommand {

    final Logger logger = LoggerFactory.getLogger(ReplaceStorageCommand.class);

    private String indexTableName;

    public ReplaceStorageCommand(String indexTableName) {
        this.indexTableName = indexTableName;
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
