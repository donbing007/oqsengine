package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * 原始对象的批量删除.
 *
 * @author dongbin
 * @version 0.1 2021/3/3 11:41
 * @since 1.8
 */
public class OriginEntitiesDeleteIndexExecutor extends AbstractJdbcTaskExecutor<Collection<OqsEngineEntity>, Integer> {

    public static OriginEntitiesDeleteIndexExecutor builder(String indexName, TransactionResource tr) {
        return new OriginEntitiesDeleteIndexExecutor(indexName, tr);
    }

    public OriginEntitiesDeleteIndexExecutor(String indexName, TransactionResource transactionResource) {
        super(indexName, transactionResource);
    }

    @Override
    public Integer execute(Collection<OqsEngineEntity> originalEntities) throws SQLException {
        final String sql = buildSql(originalEntities.size());
        int point = 1;
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            for (OqsEngineEntity entity : originalEntities) {
                st.setLong(point++, entity.getId());
            }

            st.executeUpdate();
        }

        return originalEntities.size();
    }

    private String buildSql(int size) {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ")
            .append(getTableName())
            .append(" WHERE ")
            .append(FieldDefine.ID)
            .append(" IN (")
            .append(String.join(",", Collections.nCopies(size, "?")))
            .append(")");
        return sql.toString();
    }

}
