package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 对象存在与否判断器.
 *
 * @author dongbin
 * @version 0.1 2021/2/20 14:50
 * @since 1.8
 */
public class ExistExecutor extends AbstractJdbcTaskExecutor<Long, Integer> {

    public static Executor<Long, Integer> build(String tableName, TransactionResource resource, long timeoutMs) {
        return new ExistExecutor(tableName, resource, timeoutMs);
    }

    public ExistExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public ExistExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Integer execute(Long id) throws Exception {
        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setLong(1, id);
            st.setBoolean(2, false);

            checkTimeout(st);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {

                    return rs.getInt(FieldDefine.VERSION);

                } else {

                    return -1;

                }
            }
        }

    }

    private String buildSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
            .append(FieldDefine.VERSION)
            .append(" FROM ")
            .append(getTableName())
            .append(" WHERE ")
            .append(FieldDefine.ID).append(" = ?")
            .append(" AND ")
            .append(FieldDefine.DELETED).append(" = ?");

        return sql.toString();
    }
}
