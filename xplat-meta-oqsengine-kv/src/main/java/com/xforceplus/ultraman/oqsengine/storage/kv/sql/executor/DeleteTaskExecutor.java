package com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.SqlTemplateDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * 删除任务.
 *
 * @author dongbin
 * @version 0.1 2021/07/20 23:14
 * @since 1.8
 */
public class DeleteTaskExecutor extends AbstractJdbcTaskExecutor<String[], Long> {
    public DeleteTaskExecutor(String tableName,
                              TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public DeleteTaskExecutor(String tableName,
                              TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Long execute(String[] keys) throws SQLException {
        String sql = String.format(SqlTemplateDefine.DELETE_TEMPLATE, getTableName());

        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {
            checkTimeout(ps);

            final int onlyOne = 1;
            final int first = 0;
            if (keys.length == onlyOne) {

                ps.setString(1, keys[first]);

                return Long.valueOf(ps.executeUpdate());
            } else {

                for (String key : keys) {
                    ps.setString(1, key);
                    ps.addBatch();
                }

                return Arrays.stream(ps.executeBatch()).count();
            }
        }
    }
}
