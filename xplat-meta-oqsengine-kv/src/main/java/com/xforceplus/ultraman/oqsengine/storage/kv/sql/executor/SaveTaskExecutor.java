package com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.SqlTemplateDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

    private boolean add = false;

    public SaveTaskExecutor(String tableName,
                            TransactionResource<Connection> resource, boolean add) {
        super(tableName, resource);
        this.add = add;
    }

    public SaveTaskExecutor(String tableName,
                            TransactionResource<Connection> resource, long timeoutMs, boolean add) {
        super(tableName, resource, timeoutMs);
        this.add = add;
    }

    @Override
    public Long execute(Collection<Map.Entry<String, byte[]>> kvs) throws Exception {

        String sql =
            String.format(add ? SqlTemplateDefine.INSERT_TEMPLATE : SqlTemplateDefine.REPLACE_TEMPLATE, getTableName());

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
