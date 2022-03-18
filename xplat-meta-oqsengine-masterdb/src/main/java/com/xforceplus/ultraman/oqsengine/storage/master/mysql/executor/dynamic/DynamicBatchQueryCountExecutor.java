package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.AbstractMasterTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 批量数量查询执行器.
 *
 * @author xujia 2020/11/18
 * @since 1.8
 */
public class DynamicBatchQueryCountExecutor extends AbstractMasterTaskExecutor<Long, Integer> {

    private long startTime;
    private long endTime;
    private IEntityClass entityClass;

    /**
     * 实例化.
     *
     * @param tableName   表名.
     * @param resource    事务资源.
     * @param timeout     超时毫秒.
     * @param startTime   开始时间.
     * @param endTime     结束时间.
     */
    public DynamicBatchQueryCountExecutor(String tableName, TransactionResource<Connection> resource, long timeout,
                                          long startTime, long endTime) {
        super(tableName, resource, timeout);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * 构造实例.
     *
     * @param tableName 表名.
     * @param resource 事务资源.
     * @param timeout 超时时间.
     * @param entityClass 元信息.
     * @param startTime 开始时间.
     * @param endTime 结束时间.
     * @return 执行器实例.
     */
    public static Executor<Long, Integer> build(
        String tableName, TransactionResource resource, long timeout,
        IEntityClass entityClass, long startTime, long endTime) {
        DynamicBatchQueryCountExecutor executor = new DynamicBatchQueryCountExecutor(
            tableName, resource, timeout, startTime, endTime);
        executor.setEntityClass(entityClass);
        return executor;
    }

    @Override
    public Integer execute(Long nothing) throws Exception {
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

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    private String buildCountSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) AS count")
            .append(" FROM ")
            .append(getTableName())
            .append(" WHERE ")
            .append(EntityClassHelper.buildEntityClassQuerySql(getEntityClass()))
            .append(" AND ")
            .append(FieldDefine.DELETED).append(" = ").append("?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" >= ").append("?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" <= ").append("?");

        return sql.toString();
    }
}
