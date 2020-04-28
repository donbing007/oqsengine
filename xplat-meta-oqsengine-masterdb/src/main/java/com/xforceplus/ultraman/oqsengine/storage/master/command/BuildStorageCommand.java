package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.xforceplus.ultraman.oqsengine.storage.master.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.UndoStorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpType;
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
public class BuildStorageCommand extends UndoStorageCommand<StorageEntity> {

    final Logger logger = LoggerFactory.getLogger(BuildStorageCommand.class);

    private Selector<String> tableNameSelector;

    public BuildStorageCommand(Selector<String> tableNameSelector){
        this.tableNameSelector = tableNameSelector;
    }

    @Override
    public StorageEntity execute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        super.prepareUndoLog(resource, OpType.BUILD, storageEntity);

        return this.doExecute(resource, storageEntity);
    }

    StorageEntity doExecute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        String tableName = tableNameSelector.select(Long.toString(storageEntity.getId()));
        String sql = String.format(SQLConstant.BUILD_SQL, tableName);

        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
        // id, entity, version, time, pref, cref, deleted, attribute,refs
        st.setLong(1, storageEntity.getId()); // id
        st.setLong(2, storageEntity.getEntity()); // entity
        st.setInt(3, 0); // version
        st.setLong(4, System.currentTimeMillis()); // time
        st.setLong(5, storageEntity.getPref()); // pref
        st.setLong(6, storageEntity.getCref()); // cref
        st.setBoolean(7, false); // deleted
        st.setString(8, storageEntity.getAttribute()); // attribute

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        int size = st.executeUpdate();

        /**
         * 插入影响条件恒定为1.
         */
        final int onlyOne = 1;
        if (size != onlyOne) {
            throw new SQLException(
                    String.format("Entity{%s} could not be created successfully.", storageEntity.toString()));
        }

        try {
            return null;
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

    @Override
    public StorageEntity executeUndo(TransactionResource resource, StorageEntity data) throws SQLException {
        String tableName = tableNameSelector.select(Long.toString(data.getId()));
        String sql = String.format(SQLConstant.UNDO_BUILD_SQL, tableName);

        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
        st.setLong(1, data.getId()); // id

        int size = st.executeUpdate();

        /**
         * 插入影响条件恒定为1.
         */
        final int onlyOne = 1;
        if (size != onlyOne) {
            throw new SQLException(
                    String.format("Entity{%s} undo build failed.", data.toString()));
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
