package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.AbstractMasterTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.MasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
public class DynamicBatchQueryExecutor extends AbstractMasterTaskExecutor<Long, Collection<MasterStorageEntity>> {

    private long startTime;
    private long endTime;
    private int pageSize;
    private IEntityClass entityClass;

    /**
     * 实例.
     *
     * @param tableName   表名.
     * @param resource    事务资源.
     * @param timeout     超时毫秒.
     * @param startTime   开始时间.
     * @param endTime     结束时间.
     * @param pageSize    分页大小.
     */
    public DynamicBatchQueryExecutor(String tableName,
                                     TransactionResource<Connection> resource,
                                     long timeout,
                                     long startTime,
                                     long endTime,
                                     int pageSize) {
        super(tableName, resource, timeout);
        this.startTime = startTime;
        this.endTime = endTime;
        this.pageSize = pageSize;
    }

    /**
     * 批量查询器构造器.
     */
    public static Executor<Long, Collection<MasterStorageEntity>> build(
        String tableName, TransactionResource resource, long timeout, IEntityClass entityClass,
        long startTime, long endTime, int pageSize) {
        DynamicBatchQueryExecutor executor = new DynamicBatchQueryExecutor(
            tableName, resource, timeout, startTime, endTime, pageSize);
        executor.setEntityClass(entityClass);
        return executor;
    }

    @Override
    public Collection<MasterStorageEntity> execute(Long startId) throws Exception {
        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {

            int pos = 1;
            st.setBoolean(pos++, false);
            st.setLong(pos++, startTime);
            st.setLong(pos++, endTime);
            st.setLong(pos++, startId);
            st.setLong(pos, pageSize);

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

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
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
            .append("force index(rebuild_index)")
            .append(" WHERE ")
            .append(EntityClassHelper.buildEntityClassQuerySql(entityClass))
            .append(" AND ")
            .append(FieldDefine.DELETED).append(" = ").append("?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" >= ").append("?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" <= ").append("?")
            .append(" AND ")
            .append(FieldDefine.ID).append(" > ").append("?");

        sql.append(" ORDER BY (id+0) asc ").append("LIMIT ").append("?");
        return sql.toString();
    }
}
