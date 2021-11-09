package com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor;

import com.xforceplus.ultraman.oqsengine.common.hash.Time33Hash;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.SqlTemplateDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 更新已存在数字类型KV的值.
 *
 * @author dongbin
 * @version 0.1 2021/08/18 15:21
 * @since 1.8
 */
public class IncrTaskExecutor extends AbstractJdbcTaskExecutor<Long, Long> {

    private String key;

    /**
     * 自增任务.
     *
     * @param tableName 表名.
     * @param resource  资源.
     * @param timeoutMs 超时.
     * @param key       目标key.
     */
    public IncrTaskExecutor(String tableName,
                            TransactionResource<Connection> resource, long timeoutMs, String key) {
        super(tableName, resource, timeoutMs);
        this.key = key;
    }

    @Override
    public Long execute(Long step) throws Exception {
        String updateSql = String.format(SqlTemplateDefine.UPDATE_NUMBER_TEMPLATE, getTableName());

        int size = 0;

        try (PreparedStatement ps = getResource().value().prepareStatement(updateSql)) {
            checkTimeout(ps);

            ps.setLong(1, step);
            ps.setString(2, key);
            ps.setLong(3, Time33Hash.getInstance().hash(key));

            size = ps.executeUpdate();
        }

        if (size == 0) {
            String insertSql = String.format(SqlTemplateDefine.INSERT_TEMPLATE, getTableName());
            try (PreparedStatement ps = getResource().value().prepareStatement(insertSql)) {
                checkTimeout(ps);
                ps.setString(1, key);
                ps.setLong(2, Time33Hash.getInstance().hash(key));
                ps.setLong(3, step);

                ps.executeUpdate();
            }
            return step;

        } else {

            long newValue;
            String selectSql = String.format(SqlTemplateDefine.SELECT_NUMBER_TEMPLATE, getTableName());
            try (PreparedStatement ps = getResource().value().prepareStatement(selectSql)) {
                checkTimeout(ps);

                ps.setString(1, key);
                ps.setLong(2, Time33Hash.getInstance().hash(key));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        newValue = rs.getLong(FieldDefine.VALUE);
                    } else {
                        newValue = 0;
                    }
                }
            }
            return newValue;

        }
    }
}
