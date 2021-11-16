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
import java.util.Map;

/**
 * 保存任务.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 11:29
 * @since 1.8
 */
public class SaveTaskExecutor extends AbstractJdbcTaskExecutor<Collection<Map.Entry<String, Object>>, Long> {

    private boolean add = false;
    private boolean number = false;

    /**
     * 实例化.
     *
     * @param tableName 表名.
     * @param resource  资源.
     * @param add       true表示有即错误,false表示覆盖.
     * @param number    true 是一个数字,false不是.
     */
    public SaveTaskExecutor(String tableName,
                            TransactionResource<Connection> resource, boolean add, boolean number) {
        super(tableName, resource);
        this.add = add;
        this.number = number;
    }

    /**
     * 实例化.
     *
     * @param tableName 表名.
     * @param resource  资源.
     * @param timeoutMs 操作的超时时间.
     * @param add       true表示有即错误,false表示覆盖.
     * @param number    true 是一个数字,false不是.
     */
    public SaveTaskExecutor(String tableName,
                            TransactionResource<Connection> resource, long timeoutMs, boolean add, boolean number) {
        super(tableName, resource, timeoutMs);
        this.add = add;
        this.number = number;
    }

    @Override
    public Long execute(Collection<Map.Entry<String, Object>> kvs) throws Exception {

        String sql =
            String.format(add ? SqlTemplateDefine.INSERT_TEMPLATE : SqlTemplateDefine.REPLACE_TEMPLATE, getTableName());

        // 表示只有一个kv.
        final int single = 1;
        boolean onlyOne = kvs.size() == single;
        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {

            checkTimeout(ps);

            if (onlyOne) {
                Map.Entry<String, Object> kv = kvs.stream().findFirst().get();
                ps.setString(1, kv.getKey());
                ps.setLong(2, Time33Hash.getInstance().hash(kv.getKey()));
                setValue(ps, kv.getValue());

                return Long.valueOf(ps.executeUpdate());

            } else {

                for (Map.Entry<String, Object> kv : kvs) {
                    ps.setString(1, kv.getKey());
                    ps.setLong(2, Time33Hash.getInstance().hash(kv.getKey()));
                    setValue(ps, kv.getValue());

                    ps.addBatch();
                }

                return Arrays.stream(ps.executeBatch()).count();
            }
        }
    }

    private void setValue(PreparedStatement ps, Object value) throws SQLException {
        if (number) {
            ps.setLong(3, (long) value);
        } else {
            ps.setBytes(3, (byte[]) value);
        }
    }
}
