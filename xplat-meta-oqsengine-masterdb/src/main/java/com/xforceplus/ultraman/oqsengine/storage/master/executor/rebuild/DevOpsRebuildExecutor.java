package com.xforceplus.ultraman.oqsengine.storage.master.executor.rebuild;


import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;

/**
 * Created by justin.xu on 01/2022.
 * 只能作为重建索引用.
 *
 * @since 1.8
 */
public class DevOpsRebuildExecutor implements Executor<IEntityClass, Integer> {

    private String tableName;
    private DataSource dataSource;
    private long maintainId;
    private long startTime;
    private long endTime;

    /**
     * 实例化.
     *
     * @param tableName  表名.
     * @param dataSource dataSource.
     * @param startTime  开始时间.
     * @param endTime    结束时间.
     */
    public DevOpsRebuildExecutor(String tableName, DataSource dataSource, long maintainId, long startTime,
                                 long endTime) {
        this.tableName = tableName;
        this.dataSource = dataSource;
        this.maintainId = maintainId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static Executor<IEntityClass, Integer> build(String tableName, DataSource dataSource, long maintainId,
                                                        long startTime, long endTime) {
        return new DevOpsRebuildExecutor(tableName, dataSource, maintainId, startTime, endTime);
    }

    @Override
    public Integer execute(IEntityClass entityClass) throws Exception {

        String sql = buildSQL(entityClass);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {

            st.setLong(1, System.currentTimeMillis());
            st.setLong(2, maintainId);
            st.setLong(3, CommitHelper.getMaintainCommitId());
            st.setInt(4, OperationType.UPDATE.getValue());
            st.setLong(5, startTime);
            st.setLong(6, endTime);
            st.setBoolean(7, false);

            return st.executeUpdate();
        }
    }

    private String buildSQL(IEntityClass entityClass) {
        //  "update %s set updatetime = ?, tx = ?, commitid = ?, op = ? where entityclassid = ? and updatetime >= ? and updatetime <= ?";

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableName)
            .append(" SET ")
            .append(FieldDefine.UPDATE_TIME).append("=").append("?").append(", ")
            .append(FieldDefine.TX).append("=").append("?").append(", ")
            .append(FieldDefine.COMMITID).append("=").append("?").append(", ")
            .append(FieldDefine.OP).append("=").append("?");

        sql.append(" WHERE ")
            .append(EntityClassHelper.buildEntityClassQuerySql(entityClass))
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" >= ").append("?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" <= ").append("?")
            .append(" AND ")
            .append(FieldDefine.DELETED).append(" = ").append("?");

        return sql.toString();
    }
}
