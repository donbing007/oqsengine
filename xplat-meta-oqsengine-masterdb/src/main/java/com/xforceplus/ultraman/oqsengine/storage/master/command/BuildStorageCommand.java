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
 * 创建时间: 3/27/2020 3:41 PM
 * 功能描述:
 * 修改历史:
 */
public class BuildStorageCommand implements StorageCommand<StorageEntity,Integer> {

    final Logger logger = LoggerFactory.getLogger(BuildStorageCommand.class);

    private Selector<String> tableNameSelector;

    public BuildStorageCommand(Selector<String> tableNameSelector){
        this.tableNameSelector = tableNameSelector;
    }

    @Override
    public Integer execute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        return this.doExecute(resource, storageEntity);
    }

    private int doExecute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        String tableName = tableNameSelector.select(Long.toString(storageEntity.getId()));
        String sql = String.format(SQLConstant.BUILD_SQL, tableName);

        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
        // id, entity, version, time, pref, cref, deleted, attribute,refs
        // id
        st.setLong(1, storageEntity.getId());
        // entity
        st.setLong(2, storageEntity.getEntity());
        // version
        st.setInt(3, 0);
        // time
        st.setLong(4, System.currentTimeMillis());
        // pref
        st.setLong(5, storageEntity.getPref());
        // cref
        st.setLong(6, storageEntity.getCref());
        // deleted
        st.setBoolean(7, false);
        // attribute
        st.setString(8, storageEntity.getAttribute());

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        try {
            return st.executeUpdate();
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

}
