package com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.SqlTemplateDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * 保存任务.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 11:29
 * @since 1.8
 */
public class SaveTaskExecutor extends AbstractJdbcTaskExecutor<Collection<Map.Entry<String, byte[]>>, Long> {

    public SaveTaskExecutor(String tableName,
                            TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public SaveTaskExecutor(String tableName,
                            TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Long execute(Collection<Map.Entry<String, byte[]>> kvs) throws SQLException {
        String sql = String.format(SqlTemplateDefine.REPLACE_TEMPLATE, getTableName());
        // 表示只有一个kv.
        final int single = 1;
        boolean onlyOne = kvs.size() == single;
        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {

            checkTimeout(ps);

            if (onlyOne) {
                Map.Entry<String, byte[]> kv = kvs.stream().findFirst().get();
                ps.setString(1, kv.getKey());
                ps.setBytes(2, kv.getValue());

                return Long.valueOf(ps.executeUpdate());

            } else {

                for (Map.Entry<String, byte[]> kv : kvs) {
                    ps.setString(1, kv.getKey());
                    ps.setBytes(2, kv.getValue());

                    ps.addBatch();
                }

                return Arrays.stream(ps.executeBatch()).count();
            }
        }
    }
}
