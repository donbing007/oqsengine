package com.xforceplus.ultraman.oqsengine.storage.kv.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.define.SqlTemplateDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 删除任务.
 *
 * @author dongbin
 * @version 0.1 2021/07/20 23:14
 * @since 1.8
 */
public class DeleteTaskExecutor extends AbstractJdbcTaskExecutor<String, Integer> {
    public DeleteTaskExecutor(String tableName,
                              TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public DeleteTaskExecutor(String tableName,
                              TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Integer execute(String key) throws SQLException {
        String sql = String.format(SqlTemplateDefine.DELETE_TEMPLATE, getTableName());

        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {
            ps.setString(1, key);

            checkTimeout(ps);

            return ps.executeUpdate();
        }
    }
}
