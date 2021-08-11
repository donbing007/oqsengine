package com.xforceplus.ultraman.oqsengine.storage.master.executor.errors;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

/**
 * Created by justin.xu on 05/2021.
 *
 * @since 1.8
 */
public class ReplaceErrorExecutor extends AbstractJdbcTaskExecutor<ErrorStorageEntity, Integer> {

    public static Executor<ErrorStorageEntity, Integer> build(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new ReplaceErrorExecutor(tableName, resource, timeoutMs);
    }

    public ReplaceErrorExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public ReplaceErrorExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Integer execute(ErrorStorageEntity errorStorageEntity) throws Exception {
        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setLong(1, errorStorageEntity.getId());
            st.setLong(2, errorStorageEntity.getEntity());
            st.setString(3, errorStorageEntity.getErrors());
            st.setLong(4, errorStorageEntity.getExecuteTime());
            st.setLong(5, errorStorageEntity.getFixedTime());
            st.setInt(6, errorStorageEntity.getStatus());

            checkTimeout(st);

            return st.executeUpdate();
        }
    }


    private String buildSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("REPLACE INTO ").append(getTableName())
            .append(" VALUES (")
            .append(String.join(",", Collections.nCopies(6, "?")))
            .append(")");

        return sql.toString();
    }
}

