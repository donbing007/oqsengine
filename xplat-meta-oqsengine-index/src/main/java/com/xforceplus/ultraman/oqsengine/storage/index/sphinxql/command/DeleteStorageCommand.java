package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:58 PM
 * 功能描述:
 * 修改历史:
 */
public class DeleteStorageCommand implements StorageCommand<StorageEntity> {

    final Logger logger = LoggerFactory.getLogger(DeleteStorageCommand.class);

    private String indexTableName;

    public DeleteStorageCommand(String indexTableName) {
        this.indexTableName = indexTableName;
    }

    @Override
    public StorageEntity execute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        return this.doExecute(resource, storageEntity);
    }

    StorageEntity doExecute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        String sql = String.format(SQLConstant.DELETE_SQL, indexTableName);
        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
        st.setLong(1, storageEntity.getId()); // id

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        // 在事务状态,返回值恒等于0.
        st.executeUpdate();

        try {
            return null;
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }
}
