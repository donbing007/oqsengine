package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.MasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * desc :.
 * name : BatchQueryExecutor
 *
 * @author : xujia 2020/11/18
 * @since : 1.8
 */
public class BatchQueryExecutor extends AbstractMasterExecutor<Long, Collection<MasterStorageEntity>> {

    private IEntityClass entityClass;
    private long startTime;
    private long endTime;
    private int pageSize;

    /**
     * 实例.
     *
     * @param tableName 表名.
     * @param resource 事务资源.
     * @param timeout 超时毫秒.
     * @param entityClass 元信息.
     * @param startTime 开始时间.
     * @param endTime 结束时间.
     * @param pageSize 分页大小.
     */
    public BatchQueryExecutor(String tableName, TransactionResource<Connection> resource, long timeout,
                              IEntityClass entityClass, long startTime, long endTime, int pageSize) {
        super(tableName, resource, timeout);
        this.entityClass = entityClass;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pageSize = pageSize;
    }

    public static Executor<Long, Collection<MasterStorageEntity>> build(
        String tableName, TransactionResource resource, long timeout,
        IEntityClass entityClass, long startTime, long endTime, int pageSize) {
        return new BatchQueryExecutor(tableName, resource, timeout, entityClass, startTime, endTime, pageSize);
    }

    @Override
    public Collection<MasterStorageEntity> execute(Long startId) throws SQLException {
        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setBoolean(1, false);
            st.setLong(2, startTime);
            st.setLong(3, endTime);
            st.setLong(4, startId);
            st.setLong(5, pageSize);


            checkTimeout(st);

            List<MasterStorageEntity> entities = new ArrayList<>();
            MasterStorageEntity entity;
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {

                    long[] entityClassIds = new long[FieldDefine.ENTITYCLASS_LEVEL_LIST.length];
                    for (int i = 0; i < entityClassIds.length; i++) {
                        entityClassIds[i] = rs.getLong(FieldDefine.ENTITYCLASS_LEVEL_LIST[i]);
                    }

                    entity = MasterStorageEntity.Builder.anStorageEntity()
                        .withId(rs.getLong(FieldDefine.ID))
                        .withVersion(rs.getInt(FieldDefine.VERSION))
                        .withCreateTime(rs.getLong(FieldDefine.CREATE_TIME))
                        .withUpdateTime(rs.getLong(FieldDefine.UPDATE_TIME))
                        .withOp(rs.getInt(FieldDefine.OP))
                        .withTx(rs.getLong(FieldDefine.TX))
                        .withCommitid(rs.getLong(FieldDefine.COMMITID))
                        .withAttribute(rs.getString(FieldDefine.ATTRIBUTE))
                        .withProfile(rs.getString(FieldDefine.PROFILE))
                        .withEntityClasses(entityClassIds).build();

                    entities.add(entity);
                }

                return entities;
            }
        }
    }

    private String buildSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(String.join(",",
            FieldDefine.ID,
            FieldDefine.ENTITYCLASS_LEVEL_0,
            FieldDefine.ENTITYCLASS_LEVEL_1,
            FieldDefine.ENTITYCLASS_LEVEL_2,
            FieldDefine.ENTITYCLASS_LEVEL_3,
            FieldDefine.ENTITYCLASS_LEVEL_4,
            FieldDefine.VERSION,
            FieldDefine.CREATE_TIME,
            FieldDefine.UPDATE_TIME,
            FieldDefine.ATTRIBUTE,
            FieldDefine.OP,
            FieldDefine.TX,
            FieldDefine.COMMITID,
            FieldDefine.PROFILE
            )
        );

        sql.append(" FROM ")
            .append(getTableName())
            .append(" WHERE ")
            .append(FieldDefine.COMMITID).append(" >= ").append(0)
            .append(" AND ")
            .append(EntityClassHelper.buildEntityClassQuerySql(entityClass))
            .append(" AND ")
            .append(FieldDefine.DELETED).append(" = ").append("?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" >= ").append("?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" <= ").append("?")
            .append(" AND ")
            .append(FieldDefine.ID).append(" > ").append("?")
            .append(" ORDER BY id ")
            .append("LIMIT ").append("?");
        return sql.toString();
    }
}
