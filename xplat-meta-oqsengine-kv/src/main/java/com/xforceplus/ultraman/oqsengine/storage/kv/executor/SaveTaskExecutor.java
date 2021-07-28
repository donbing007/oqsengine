package com.xforceplus.ultraman.oqsengine.storage.kv.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.define.SqlTemplateDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * 保存任务.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 11:29
 * @since 1.8
 */
public class SaveTaskExecutor extends AbstractJdbcTaskExecutor<Map.Entry<String, byte[]>, Integer> {

    public SaveTaskExecutor(String tableName,
                            TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public SaveTaskExecutor(String tableName,
                            TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Integer execute(Map.Entry<String, byte[]> kv) throws SQLException {
        String sql = String.format(SqlTemplateDefine.REPLACE_TEMPLATE, getTableName());
        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {

            checkTimeout(ps);

            ps.setString(1, kv.getKey());
            ps.setBytes(2, kv.getValue());

            this.checkTimeout(ps);

            return ps.executeUpdate();
        }
    }
}
