package com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.SqlTemplateDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 获取任务.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 15:14
 * @since 1.8
 */
public class GetTaskExecutor extends AbstractJdbcTaskExecutor<String, byte[]> {
    public GetTaskExecutor(String tableName,
                           TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public GetTaskExecutor(String tableName,
                           TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public byte[] execute(String key) throws SQLException {
        String sql = String.format(SqlTemplateDefine.SELECT_TEMPLATE, getTableName());

        byte[] data = null;
        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {

            checkTimeout(ps);

            ps.setString(1, key);


            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data = rs.getBytes(FieldDefine.VALUE);
                    break;
                }
            }
        }

        return data;
    }
}
