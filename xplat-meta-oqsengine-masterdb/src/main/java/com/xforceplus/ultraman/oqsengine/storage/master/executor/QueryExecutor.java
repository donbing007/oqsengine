package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class QueryExecutor implements Executor<Long, Optional<StorageEntity>> {

    final Logger logger = LoggerFactory.getLogger(QueryExecutor.class);

    private Selector<String> tableNameSelector;
    private TransactionResource<Connection> resource;
    private boolean noDetail;

    /**
     * 查询包含详细信息
     *
     * @param tableNameSelector
     * @param resource
     * @return
     */
    public static Executor<Long, Optional<StorageEntity>> buildHaveDetail(
        Selector<String> tableNameSelector, TransactionResource resource) {
        return new QueryExecutor(tableNameSelector, resource, false);
    }

    /**
     * 查询不包含详细信息.只有版本和事务信息.
     *
     * @param tableNameSelector
     * @param resource
     * @return
     */
    public static Executor<Long, Optional<StorageEntity>> buildNoDetail(
        Selector<String> tableNameSelector, TransactionResource resource) {
        return new QueryExecutor(tableNameSelector, resource, true);
    }

    public QueryExecutor(
        Selector<String> tableNameSelector, TransactionResource<Connection> resource, boolean noDetail) {
        this.tableNameSelector = tableNameSelector;
        this.resource = resource;
        this.noDetail = noDetail;
    }

    @Override
    public Optional<StorageEntity> execute(Long id) throws SQLException {
        String sql = buildSQL(id);
        PreparedStatement st = resource.value().prepareStatement(sql);
        st.setLong(1, id);
        st.setBoolean(2, false);

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

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
        if (!noDetail) {
            sql.append(String.join(",",
                FieldDefine.ENTITY,
                FieldDefine.VERSION,
                FieldDefine.PREF,
                FieldDefine.CREF,
                FieldDefine.TIME,
                FieldDefine.TX,
                FieldDefine.COMMITID,
                FieldDefine.ATTRIBUTE
                )
            );
        } else {
            sql.append(String.join(",",
                FieldDefine.VERSION,
                FieldDefine.TIME,
                FieldDefine.TX,
                FieldDefine.COMMITID
                )
            );
        }

        sql.append(" FROM ")
            .append(tableNameSelector.select(Long.toString(id)))
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("?")
            .append(" AND ")
            .append(FieldDefine.DELETED).append("=").append("?");
        return sql.toString();
    }
}
