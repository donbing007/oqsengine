package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
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
 * build executor
 */
public class BuildExecutor implements Executor<StorageEntity, Integer> {

    final Logger logger = LoggerFactory.getLogger(BuildExecutor.class);

    private String indexTableName;

    private String buildSql;

    private TransactionResource resource;

    public BuildExecutor(TransactionResource resource, String indexTableName) {
        this.indexTableName = indexTableName;
        this.resource = resource;
        buildSql =
                String.format(SQLConstant.WRITER_SQL,
                        "insert", indexTableName,
                        FieldDefine.ID, FieldDefine.ENTITY, FieldDefine.PREF, FieldDefine.CREF,
                        FieldDefine.TX, FieldDefine.COMMIT_ID ,
                        FieldDefine.JSON_FIELDS, FieldDefine.FULL_FIELDS);
    }

    public static BuildExecutor build(TransactionResource resource, String indexTableName){
        return new BuildExecutor(resource, indexTableName);
    }


    @Override
    public Integer execute(StorageEntity storageEntity) throws SQLException {
        final String sql = String.format(buildSql, indexTableName);

        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);

        // id, entity, pref, cref, tx, commit, jsonfileds, fullfileds
        // id
        st.setLong(1, storageEntity.getId());
        // entity
        st.setLong(2, storageEntity.getEntity());
        // pref
        st.setLong(3, storageEntity.getPref());
        // cref
        st.setLong(4, storageEntity.getCref());
        //tx
        st.setLong(5, storageEntity.getTx());
        //commitid
        st.setLong(6, storageEntity.getCommitId());
        // jsonfileds
        st.setString(7, SphinxQLHelper.serializableJson(storageEntity.getJsonFields()));
        // fullfileds
        st.setString(8, toFullString(storageEntity.getFullFields()));

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
