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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Query Executor by One
 */
public class QueryExecutor implements Executor<Long, Optional<StorageEntity>> {

    final Logger logger = LoggerFactory.getLogger(QueryExecutor.class);

    private String indexTableName;

    private TransactionResource resource;

    public QueryExecutor(String indexTableName, TransactionResource resource) {
        this.indexTableName = indexTableName;
        this.resource = resource;
    }

    public static QueryExecutor build(TransactionResource resource, String indexTableName){
        return new QueryExecutor(indexTableName, resource);
    }

    @Override
    public Optional<StorageEntity> execute(Long id) throws SQLException {
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
                        rs.getLong(FieldDefine.TX),
                        rs.getLong(FieldDefine.COMMIT_ID),
                        SphinxQLHelper.deserializeJson(rs.getString(FieldDefine.JSON_FIELDS)),
                        null
                );
            }

            return Optional.ofNullable(storageEntity);
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
