package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.CommonUtil.toFullString;

/**
 * Replace executor
 */
public class ReplaceExecutor implements Executor<StorageEntity, Integer> {

    final Logger logger = LoggerFactory.getLogger(ReplaceExecutor.class);

    private String indexTableName;

    private String replaceSql;

    private TransactionResource resource;

    public ReplaceExecutor(TransactionResource resource, String indexTableName) {
        this.indexTableName = indexTableName;
        this.resource = resource;
        replaceSql =
            String.format(SQLConstant.WRITER_SQL,
                "replace", indexTableName,
                FieldDefine.ID,
                FieldDefine.ENTITY,
                FieldDefine.ENTITY_F,
                FieldDefine.PREF,
                FieldDefine.CREF,
                FieldDefine.TX,
                FieldDefine.COMMIT_ID,
                FieldDefine.JSON_FIELDS,
                FieldDefine.FULL_FIELDS,
                FieldDefine.MAINTAIN_ID,
                FieldDefine.TIME,
                FieldDefine.OQS_MAJOR);
    }

    public static ReplaceExecutor build(TransactionResource resource, String indexTableName) {
        return new ReplaceExecutor(resource, indexTableName);
    }

    @Override
    public Integer execute(StorageEntity storageEntity) throws SQLException {

        final String sql = String.format(replaceSql, indexTableName);

        try (PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql)) {

            // id, entity, pref, cref, tx, commit, jsonfileds, fullfileds
            // id
            st.setLong(1, storageEntity.getId());
            // entity
            st.setLong(2, storageEntity.getEntity());
            // entityf
            st.setString(3, Long.toString(storageEntity.getEntity()));
            // pref
            st.setLong(4, storageEntity.getPref());
            // cref
            st.setLong(5, storageEntity.getCref());
            //tx
            st.setLong(6, storageEntity.getTx());
            //commitid
            st.setLong(7, storageEntity.getCommitId());
            // jsonfileds
            st.setString(8, SphinxQLHelper.serializableJson(storageEntity.getJsonFields()));
            // fullfileds
            st.setString(9, toFullString(storageEntity.getFullFields()));
            // maintainId
            st.setLong(10, storageEntity.getMaintainId());
            // time
            st.setLong(11, storageEntity.getTime());
            // oqsmajor
            st.setInt(12, OqsVersion.MAJOR);

            return st.executeUpdate();
        }
    }
}
