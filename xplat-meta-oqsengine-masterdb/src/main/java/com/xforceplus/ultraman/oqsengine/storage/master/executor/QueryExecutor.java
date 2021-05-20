package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.MasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * 查询指定对象.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 16:32
 * @since 1.8
 */
public class QueryExecutor extends AbstractMasterExecutor<Long, Optional<MasterStorageEntity>> {

    private boolean noDetail;

    /**
     * 查询所有信息.
     *
     * @param tableName 表名.
     * @param resource  事务资源.
     * @param timeoutMs 超时毫秒.
     * @return 执行器实例.
     */
    public static Executor<Long, Optional<MasterStorageEntity>> buildHaveAllDetail(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new QueryExecutor(tableName, resource, false, timeoutMs);
    }

    /**
     * 查询包含详细信息.
     *
     * @param tableName 表名.
     * @param resource  事务资源.
     * @return 执行器实例.
     */
    public static Executor<Long, Optional<MasterStorageEntity>> buildHaveDetail(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new QueryExecutor(tableName, resource, false, timeoutMs);
    }

    /**
     * 查询不包含详细信息.只有版本和事务信息.
     *
     * @param tableName 表名.
     * @param resource  事务资源.
     * @return 执行器实例.
     */
    public static Executor<Long, Optional<MasterStorageEntity>> buildNoDetail(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new QueryExecutor(tableName, resource, true, timeoutMs);
    }

    public QueryExecutor(String tableName, TransactionResource<Connection> resource, boolean noDetail) {
        this(tableName, resource, noDetail, 0);
    }

    /**
     * 构造实例.
     *
     * @param tableName 表名.
     * @param resource  事务资源.
     * @param noDetail  true不需要详细信息, false需要详细信息.
     * @param timeoutMs 超时毫秒.
     */
    public QueryExecutor(
        String tableName,
        TransactionResource<Connection> resource,
        boolean noDetail,
        long timeoutMs) {
        super(tableName, resource, timeoutMs);
        this.noDetail = noDetail;
    }

    @Override
    public Optional<MasterStorageEntity> execute(Long id) throws SQLException {
        String sql = buildSQL(id);
        try (PreparedStatement st = getResource().value().prepareStatement(
            sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

            st.setFetchSize(Integer.MIN_VALUE);
            st.setLong(1, id);
            st.setBoolean(2, false);

            checkTimeout(st);

            MasterStorageEntity entity = null;

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    MasterStorageEntity.Builder storageEntityBuilder = MasterStorageEntity.Builder.anStorageEntity()
                        .withId(id)
                        .withVersion(rs.getInt(FieldDefine.VERSION))
                        .withCreateTime(rs.getLong(FieldDefine.CREATE_TIME))
                        .withUpdateTime(rs.getLong(FieldDefine.UPDATE_TIME))
                        .withOp(rs.getInt(FieldDefine.OP));

                    long[] entityClassIds = new long[FieldDefine.ENTITYCLASS_LEVEL_LIST.length];
                    for (int i = 0; i < entityClassIds.length; i++) {
                        entityClassIds[i] = rs.getLong(FieldDefine.ENTITYCLASS_LEVEL_LIST[i]);
                    }
                    storageEntityBuilder.withEntityClasses(entityClassIds);
                    storageEntityBuilder.withProfile(rs.getString(FieldDefine.PROFILE));

                    if (!noDetail) {
                        storageEntityBuilder.withAttribute(rs.getString(FieldDefine.ATTRIBUTE));
                    }
                    entity = storageEntityBuilder.build();
                }

                return Optional.ofNullable(entity);
            }
        }
    }

    private String buildSQL(long id) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(String.join(",",
            FieldDefine.ENTITYCLASS_LEVEL_0,
            FieldDefine.ENTITYCLASS_LEVEL_1,
            FieldDefine.ENTITYCLASS_LEVEL_2,
            FieldDefine.ENTITYCLASS_LEVEL_3,
            FieldDefine.ENTITYCLASS_LEVEL_4,
            FieldDefine.VERSION,
            FieldDefine.CREATE_TIME,
            FieldDefine.UPDATE_TIME,
            FieldDefine.OQS_MAJOR,
            FieldDefine.OP,
            FieldDefine.PROFILE
            )
        );
        if (!noDetail) {
            sql.append(",")
                .append(String.join(",",
                    FieldDefine.ATTRIBUTE
                    )
                );
        }

        sql.append(" FROM ")
            .append(getTableName())
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("?")
            .append(" AND ")
            .append(FieldDefine.DELETED).append("=").append("?");

        return sql.toString();
    }
}
