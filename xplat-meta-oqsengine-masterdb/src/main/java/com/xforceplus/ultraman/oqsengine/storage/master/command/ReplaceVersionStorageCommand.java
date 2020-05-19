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
 * 创建时间: 4/17/2020 4:40 PM
 * 功能描述:
 * 修改历史:
 */
public class ReplaceVersionStorageCommand  implements StorageCommand<StorageEntity> {
    final Logger logger = LoggerFactory.getLogger(ReplaceVersionStorageCommand.class);

    private Selector<String> tableNameSelector;

    public ReplaceVersionStorageCommand(Selector<String> tableNameSelector){
        this.tableNameSelector = tableNameSelector;
    }

    @Override
    public StorageEntity execute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        return this.doExecute(resource, storageEntity);
    }

    StorageEntity doExecute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        String tableName = tableNameSelector.select(Long.toString(storageEntity.getId()));
        String sql = String.format(SQLConstant.REPLACE_VERSION_TIME_SQL, tableName);


        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
        st.setInt(1, storageEntity.getVersion());
        st.setLong(2, storageEntity.getTime());
        st.setLong(3, storageEntity.getId());

        int size = st.executeUpdate();

        final int onlyOne = 1;
        if (size != onlyOne) {
            throw new SQLException(
                    String.format("Unable to synchronize information to %d.", storageEntity.getId()));
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
