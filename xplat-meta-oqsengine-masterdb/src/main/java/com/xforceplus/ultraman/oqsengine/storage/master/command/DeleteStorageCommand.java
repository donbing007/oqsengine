package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.xforceplus.ultraman.oqsengine.storage.master.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
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
 * 创建时间: 3/27/2020 4:12 PM
 * 功能描述:
 * 修改历史:
 */
public class DeleteStorageCommand implements StorageCommand<StorageEntity> {

    final Logger logger = LoggerFactory.getLogger(ReplaceStorageCommand.class);

    private Selector<String> tableNameSelector;

    public DeleteStorageCommand(Selector<String> tableNameSelector){
        this.tableNameSelector = tableNameSelector;
    }

    @Override
    public StorageEntity execute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        return this.doExecute(resource, storageEntity);
    }

    StorageEntity doExecute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        String tableName = tableNameSelector.select(Long.toString(storageEntity.getId()));
        String sql = String.format(SQLConstant.DELETE_SQL, tableName);
        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);

        // deleted time id version;
        st.setBoolean(1, true); // deleted
        st.setLong(2, System.currentTimeMillis()); // time
        st.setLong(3, storageEntity.getId()); // id
        st.setInt(4, storageEntity.getVersion()); // version

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        int size = st.executeUpdate();
        final int onlyOne = 1;
        if (size != onlyOne) {
            throw new SQLException(String.format("Entity{%s} could not be delete successfully.", storageEntity.toString()));
        }

        try {
            return null;
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }
}
