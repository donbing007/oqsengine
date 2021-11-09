package com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor;

import com.xforceplus.ultraman.oqsengine.common.hash.Time33Hash;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.SqlTemplateDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

/**
 * 删除任务.
 *
 * @author dongbin
 * @version 0.1 2021/07/20 23:14
 * @since 1.8
 */
public class DeleteTaskExecutor extends AbstractJdbcTaskExecutor<Collection<String>, Long> {
    public DeleteTaskExecutor(String tableName,
                              TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public DeleteTaskExecutor(String tableName,
                              TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Long execute(Collection<String> keys) throws SQLException {
        String sql = String.format(SqlTemplateDefine.DELETE_TEMPLATE, getTableName());

        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {
            checkTimeout(ps);

            final int onlyOne = 1;
            if (keys.size() == onlyOne) {

                String key = keys.stream().findFirst().get();
                ps.setString(1, key);
                ps.setLong(2, Time33Hash.getInstance().hash(key));

                return Long.valueOf(ps.executeUpdate());
            } else {

                for (String key : keys) {
                    ps.setString(1, key);
                    ps.setLong(2, Time33Hash.getInstance().hash(key));
                    ps.addBatch();
                }

                return Arrays.stream(ps.executeBatch()).count();
            }
        }
    }
}
