package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.original;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.MapAttributeMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 静态对象的单实例查询.
 *
 * @author dongbin
 * @version 0.1 2022/2/24 11:16
 * @since 1.8
 */
public class OriginalQueryExecutor extends
    AbstractOriginalTaskExecutor<Long, Optional<MapAttributeMasterStorageEntity>> {

    private boolean noDetail;

    public OriginalQueryExecutor(String tableName, TransactionResource<Connection> resource, boolean noDetail) {
        this(tableName, resource, noDetail, 0);
    }

    public OriginalQueryExecutor(
        String tableName, TransactionResource<Connection> resource, boolean noDetail, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Optional<MapAttributeMasterStorageEntity> execute(Long id) throws Exception {
        String controlSql = buildControlSQL();
        MapAttributeMasterStorageEntity storageEntity = null;
        try (PreparedStatement ps = getResource().value().prepareStatement(controlSql)) {

            ps.setLong(1, id);
            ps.setBoolean(2, false);

            checkTimeout(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    storageEntity = new MapAttributeMasterStorageEntity();
                    storageEntity.setId(id);

                    storageEntity.setVersion(rs.getInt(FieldDefine.VERSION));
                    storageEntity.setCreateTime(rs.getLong(FieldDefine.CREATE_TIME));
                    storageEntity.setUpdateTime(rs.getLong(FieldDefine.UPDATE_TIME));
                    storageEntity.setOp(rs.getInt(FieldDefine.OP));
                    storageEntity.setProfile(rs.getString(FieldDefine.PROFILE));

                    long[] entityClassIds = new long[FieldDefine.ENTITYCLASS_LEVEL_LIST.length];
                    for (int i = 0; i < entityClassIds.length; i++) {
                        entityClassIds[i] = rs.getLong(FieldDefine.ENTITYCLASS_LEVEL_LIST[i]);
                    }
                    storageEntity.setEntityClasses(entityClassIds);
                }
            }
        }

        if (storageEntity == null) {
            return Optional.empty();
        }

        Optional<IEntityClass> entityClassOp =
            findEntityClass(storageEntity.getEntityClasses(), storageEntity.getProfile());
        IEntityClass entityClass;
        if (!entityClassOp.isPresent()) {
            return Optional.empty();
        } else {
            entityClass = entityClassOp.get();
        }

        if (!noDetail) {
            // 需要加载详细信息.
            String attributeSql = buildAttributeSql(entityClass);
            try (PreparedStatement ps = getResource().value().prepareStatement(attributeSql)) {

                ps.setLong(1, id);

                checkTimeout(ps);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        entityClass.fields().forEach(f -> {

                        });
                    }

                }
            }
        }

        return Optional.ofNullable(storageEntity);
    }

    private String buildAttributeSql(IEntityClass entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        //准备返回字段.
        sql.append(entityClass.fields().stream().map(f -> f.name()).collect(Collectors.joining(", ")));

        sql.append(" FROM ")
            .append(buildTableName(entityClass))
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("?");
        return sql.toString();
    }

    private String buildControlSQL() {
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

        sql.append(" FROM ")
            .append(getTableName())
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("?")
            .append(" AND ")
            .append(FieldDefine.DELETED).append("=").append("?");

        return sql.toString();
    }
}
