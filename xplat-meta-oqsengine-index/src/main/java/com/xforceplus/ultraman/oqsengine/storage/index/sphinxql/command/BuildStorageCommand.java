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
import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.CommonUtil.toFullString;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:58 PM
 * 功能描述:
 * 修改历史:
 */
public class BuildStorageCommand implements StorageCommand<StorageEntity, Integer> {

    final Logger logger = LoggerFactory.getLogger(BuildStorageCommand.class);

    private String indexTableName;

    private String buildSql;

    public BuildStorageCommand(String indexTableName) {
        this.indexTableName = indexTableName;

        buildSql =
            String.format(SQLConstant.WRITER_SQL,
                "insert", indexTableName,
                FieldDefine.ID, FieldDefine.ENTITY, FieldDefine.PREF, FieldDefine.CREF,
                FieldDefine.JSON_FIELDS, FieldDefine.FULL_FIELDS);
    }

    @Override
    public Integer execute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {

        return this.doExecute(resource, storageEntity);
    }

    private int doExecute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        final String sql = String.format(buildSql, indexTableName);

        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);

        // id, entity, pref, cref, jsonfileds, fullfileds
        // id
        st.setLong(1, storageEntity.getId());
        // entity
        st.setLong(2, storageEntity.getEntity());
        // pref
        st.setLong(3, storageEntity.getPref());
        // cref
        st.setLong(4, storageEntity.getCref());
        // jsonfileds
        st.setString(5, SphinxQLHelper.serializableJson(storageEntity.getJsonFields()));
        // fullfileds
        st.setString(6, toFullString(storageEntity.getFullFields()));

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        try {

            return st.executeUpdate();
        } finally {
            st.close();
        }
    }
}
