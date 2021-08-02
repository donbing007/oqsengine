package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.StorageUniqueEntity;
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
public class QueryUniqueExecutor extends AbstractJdbcTaskExecutor<String, Optional<StorageUniqueEntity>> {

    private IEntityClass entityClass;


    public QueryUniqueExecutor(String tableName, TransactionResource<Connection> resource, IEntityClass entityClass) {
        this(tableName, resource, entityClass, 0);
    }

    public QueryUniqueExecutor(
        String tableName,
        TransactionResource<Connection> resource,
        IEntityClass entityClass,
        long timeoutMs) {
        super(tableName, resource, timeoutMs);
        this.entityClass = entityClass;
    }

    @Override
    public Optional<StorageUniqueEntity> execute(String key) throws SQLException {
        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(
            sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            st.setFetchSize(Integer.MIN_VALUE);
            st.setString(1, key);
            st.setLong(2, entityClass.id());
            checkTimeout(st);
            StorageUniqueEntity entity = null;
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    StorageUniqueEntity.StorageUniqueEntityBuilder builder =
                        new StorageUniqueEntity.StorageUniqueEntityBuilder();
                    builder.key(key).id(rs.getLong(FieldDefine.ID));
                    long[] entityClassIds = new long[FieldDefine.ENTITYCLASS_LEVEL_LIST.length];
                    for (int i = 0; i < entityClassIds.length; i++) {
                        entityClassIds[i] = rs.getLong(FieldDefine.ENTITYCLASS_LEVEL_LIST[i]);
                    }
                    builder.entityClasses(entityClassIds);
                    entity = builder.build();
                }
                return Optional.ofNullable(entity);
            }
        }
    }

    private String buildSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id,entityclassl0,entityclassl1,entityclassl2,entityclassl3,entityclassl4");
        sql.append(" FROM ")
            .append(getTableName())
            .append(" WHERE ")
            .append(FieldDefine.UNIQUE_KEY).append("=").append("?")
            .append(" AND ");
        int level = entityClass.level();
        sql.append(FieldDefine.ENTITYCLASS_LEVEL_LIST[level]).append("=?");
        return sql.toString();
    }
}
