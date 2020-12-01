package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * desc :
 * name : BatchQueryCountExecutor
 *
 * @author : xujia
 * date : 2020/11/18
 * @since : 1.8
 */
public class BatchQueryCountExecutor extends AbstractMasterExecutor<Long, Optional<Integer>> {

    private long entity;
    private long startTime;
    private long endTime;

    public BatchQueryCountExecutor(String tableName, TransactionResource<Connection> resource, long timeout,
                                   long entity, long startTime, long endTime) {
        super(tableName, resource, timeout);
        this.entity = entity;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static Executor<Long, Optional<Integer>> build(
            String tableName, TransactionResource resource, long timeout,
            long entity, long startTime, long endTime) {
        return new BatchQueryCountExecutor(tableName, resource, timeout, entity, startTime, endTime);
    }

    @Override
    public Optional<Integer> execute(Long aLong) throws SQLException {
        String sql = buildCountSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setLong(1, entity);
            st.setLong(2, startTime);
            st.setLong(3, endTime);

            checkTimeout(st);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt(1));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    private String buildCountSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) ");
        sql.append(" FROM ")
                .append(getTableName())
                .append(" WHERE ")
                .append(FieldDefine.ENTITY).append("=").append("?")
                .append(" AND ")
                .append(FieldDefine.DELETED).append("=").append("false")
                .append(" AND ")
                .append(FieldDefine.TIME).append(">=").append("?")
                .append(" AND ")
                .append(FieldDefine.TIME).append("<=").append("?");

        return sql.toString();
    }
}
