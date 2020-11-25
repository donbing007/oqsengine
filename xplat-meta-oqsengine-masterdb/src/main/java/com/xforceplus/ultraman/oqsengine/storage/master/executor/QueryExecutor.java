package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
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
public class QueryExecutor extends AbstractMasterExecutor<Long, Optional<StorageEntity>> {

    private boolean noDetail;
    private boolean noMeta;

    /**
     * 查询所有信息.
     *
     * @param tableName
     * @param resource
     * @param timeoutMs
     * @return
     */
    public static Executor<Long, Optional<StorageEntity>> buildHaveAllDetail(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new QueryExecutor(tableName, resource, false, false, timeoutMs);
    }

    /**
     * 查询包含详细信息
     *
     * @param tableName
     * @param resource
     * @return
     */
    public static Executor<Long, Optional<StorageEntity>> buildHaveDetail(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new QueryExecutor(tableName, resource, false, true, timeoutMs);
    }

    /**
     * 查询不包含详细信息.只有版本和事务信息.
     *
     * @param tableName
     * @param resource
     * @return
     */
    public static Executor<Long, Optional<StorageEntity>> buildNoDetail(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new QueryExecutor(tableName, resource, true, true, timeoutMs);
    }

    public QueryExecutor(String tableName, TransactionResource<Connection> resource, boolean noDetail) {
        super(tableName, resource);
        this.noDetail = noDetail;
    }

    public QueryExecutor(
        String tableName,
        TransactionResource<Connection> resource,
        boolean noDetail,
        boolean noMeta,
        long timeoutMs) {
        super(tableName, resource, timeoutMs);
        this.noDetail = noDetail;
        this.noMeta = noMeta;
    }

    @Override
    public Optional<StorageEntity> execute(Long id) throws SQLException {
        String sql = buildSQL(id);
        PreparedStatement st = getResource().value().prepareStatement(
            sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        st.setFetchSize(Integer.MIN_VALUE);
        st.setLong(1, id);
        st.setBoolean(2, false);

        checkTimeout(st);

        StorageEntity entity = null;
        ResultSet rs = null;
        try {
            rs = st.executeQuery();
            if (rs.next()) {
                entity = new StorageEntity();
                entity.setId(id);
                entity.setVersion(rs.getInt(FieldDefine.VERSION));
                entity.setTime(rs.getLong(FieldDefine.TIME));
                entity.setTx(rs.getLong(FieldDefine.TX));
                entity.setCommitid(rs.getLong(FieldDefine.COMMITID));
                if (!noDetail) {
                    entity.setEntity(rs.getLong(FieldDefine.ENTITY));
                    entity.setPref(rs.getLong(FieldDefine.PREF));
                    entity.setCref(rs.getLong(FieldDefine.CREF));
                    entity.setAttribute(rs.getString(FieldDefine.ATTRIBUTE));
                }
                if (!noMeta) {
                    entity.setMeta(rs.getString(FieldDefine.META));
                }
            }


            return Optional.ofNullable(entity);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
        }
    }

    private String buildSQL(long id) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(String.join(",",
            FieldDefine.VERSION,
            FieldDefine.TIME,
            FieldDefine.TX,
            FieldDefine.COMMITID
            )
        );
        if (!noDetail) {
            sql.append(",")
                .append(String.join(",",
                    FieldDefine.ENTITY,
                    FieldDefine.PREF,
                    FieldDefine.CREF,
                    FieldDefine.ATTRIBUTE
                    )
                );
        }

        if (!noMeta) {
            sql.append(",")
                .append(FieldDefine.META);
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
