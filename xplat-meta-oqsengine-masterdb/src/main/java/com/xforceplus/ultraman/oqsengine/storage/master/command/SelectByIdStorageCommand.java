package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.xforceplus.ultraman.oqsengine.storage.master.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/17/2020 4:15 PM
 * 功能描述:
 * 修改历史:
 */
public class SelectByIdStorageCommand implements StorageCommand<StorageEntity> {

    final Logger logger = LoggerFactory.getLogger(SelectByIdStorageCommand.class);

    private Selector<String> tableNameSelector;

    public SelectByIdStorageCommand(Selector<String> tableNameSelector) {
        this.tableNameSelector = tableNameSelector;
    }

    @Override
    public StorageEntity execute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        return this.doExecute(resource, storageEntity.getId());
    }

    StorageEntity doExecute(TransactionResource resource, Long id) throws SQLException {
        String tableName = tableNameSelector.select(Long.toString(id));
        String sql = String.format(SQLConstant.SELECT_SQL, tableName);

        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = ((Connection) resource.value()).prepareStatement(sql);
            st.setLong(1, id); // id

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            rs = st.executeQuery();
            // entity, version, time, pref, cref, deleted, attribute, refs

            StorageEntity storageEntity = null;
            if (rs.next()) {
                storageEntity = new StorageEntity(
                        id,
                        rs.getLong(FieldDefine.ENTITY),
                        rs.getInt(FieldDefine.VERSION),
                        rs.getLong(FieldDefine.PREF),
                        rs.getLong(FieldDefine.CREF),
                        rs.getBoolean(FieldDefine.DELETED),
                        rs.getString(FieldDefine.ATTRIBUTE)
                );
            }

            if(storageEntity == null) {
                throw new SQLException(
                         String.format("Data that does not exist.[%d]", id)
                );
            }

            return storageEntity;
        } finally {
            if (rs != null) {
                rs.close();
            }

            if (st != null) {
                st.close();
            }
        }
    }
}
