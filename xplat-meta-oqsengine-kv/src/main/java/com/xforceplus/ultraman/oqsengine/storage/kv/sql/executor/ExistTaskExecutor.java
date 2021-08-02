package com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.SqlTemplateDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 判断key是否存在的任务.
 *
 * @author dongbin
 * @version 0.1 2021/07/20 22:47
 * @since 1.8
 */
public class ExistTaskExecutor extends AbstractJdbcTaskExecutor<String, Boolean> {

    public ExistTaskExecutor(String tableName,
                             TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public ExistTaskExecutor(String tableName,
                             TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Boolean execute(String key) throws SQLException {
        String sql = String.format(SqlTemplateDefine.EXIST_TEMPLATE, getTableName());

        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {
            ps.setString(1, key);

            checkTimeout(ps);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
