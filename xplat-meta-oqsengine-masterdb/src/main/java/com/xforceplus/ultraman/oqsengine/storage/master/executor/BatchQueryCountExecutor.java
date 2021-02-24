package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * desc :
 * name : BatchQueryCountExecutor
 *
 * @author : xujia
 * date : 2020/11/18
 * @since : 1.8
 */
public class BatchQueryCountExecutor extends AbstractMasterExecutor<Long, Integer> {

    private IEntityClass entityClass;
    private long startTime;
    private long endTime;

    public BatchQueryCountExecutor(String tableName, TransactionResource<Connection> resource, long timeout,
                                   IEntityClass entityClass, long startTime, long endTime) {
        super(tableName, resource, timeout);
        this.entityClass = entityClass;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static Executor<Long, Integer> build(
        String tableName, TransactionResource resource, long timeout,
        IEntityClass entityClass, long startTime, long endTime) {
        return new BatchQueryCountExecutor(tableName, resource, timeout, entityClass, startTime, endTime);
    }

    @Override
    public Integer execute(Long aLong) throws SQLException {
        String sql = buildCountSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setBoolean(1, false);
            st.setLong(2, startTime);
            st.setLong(3, endTime);

            checkTimeout(st);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt("count");
            }
        }
    }

    private String buildCountSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) AS count")
            .append(" FROM ")
            .append(getTableName())
            .append(" WHERE ")
            // 增加commitid的条件是为了可以应用到多列索引.
            .append(FieldDefine.COMMITID).append(" >= 0")
            .append(" AND ")
            .append(EntityClassHelper.buildEntityClassQuerySql(entityClass))
            .append(" AND ")
            .append(FieldDefine.DELETED).append(" = ").append("?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" >= ").append("?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" <= ").append("?");

        return sql.toString();
    }
}
