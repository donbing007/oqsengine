package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/17/2020 4:05 PM
 * 功能描述:
 * 修改历史:
 */
public class SelectByIdStorageCommand implements StorageCommand<StorageEntity> {

    final Logger logger = LoggerFactory.getLogger(SelectByIdStorageCommand.class);

    private String indexTableName;

    public SelectByIdStorageCommand(String indexTableName) {
        this.indexTableName = indexTableName;
    }

    @Override
    public StorageEntity execute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        return this.doExecute(resource, storageEntity.getId());
    }

    StorageEntity doExecute(TransactionResource resource, Long id) throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String sql = String.format(SQLConstant.SELECT_FROM_ID_SQL, indexTableName);
            st = ((Connection) resource.value()).prepareStatement(sql);
            st.setLong(1, id);

            rs = st.executeQuery();
            StorageEntity storageEntity = null;
            if (rs.next()) {
                storageEntity = new StorageEntity(
                        id,
                        rs.getLong(FieldDefine.ENTITY),
                        rs.getLong(FieldDefine.PREF),
                        rs.getLong(FieldDefine.CREF),
                        SphinxQLHelper.deserializeJson(rs.getString(FieldDefine.JSON_FIELDS)),
                        null
                );
            }

            if (storageEntity == null) {
                throw new SQLException(
                        String.format("Attempt to update a property on a data that does not exist.[%d]", id)
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
