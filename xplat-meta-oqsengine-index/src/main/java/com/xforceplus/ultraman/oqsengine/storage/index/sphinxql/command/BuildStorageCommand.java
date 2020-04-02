package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:58 PM
 * 功能描述:
 * 修改历史:
 */
public class BuildStorageCommand implements StorageCommand {

    final Logger logger = LoggerFactory.getLogger(BuildStorageCommand.class);

    private String indexTableName;

    public BuildStorageCommand(String indexTableName) {
        this.indexTableName = indexTableName;
    }

    @Override
    public Object execute(Connection conn, Object data) throws SQLException {
        IEntity entity = (IEntity) data;
        return null;
    }

}
