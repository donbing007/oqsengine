package com.xforceplus.ultraman.oqsengine.storage.master.executor.errors;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.master.condition.QueryErrorCondition;
import com.xforceplus.ultraman.oqsengine.storage.master.define.ErrorDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.AbstractMasterExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by justin.xu on 05/2021.
 *
 * @since 1.8
 */
public class QueryErrorExecutor extends AbstractMasterExecutor<QueryErrorCondition, Collection<ErrorStorageEntity>> {

    public static Executor<QueryErrorCondition, Collection<ErrorStorageEntity>> build(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new QueryErrorExecutor(tableName, resource, timeoutMs);
    }

    public QueryErrorExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Collection<ErrorStorageEntity> execute(QueryErrorCondition queryErrorCondition) throws SQLException {
        String sql = buildSQL(queryErrorCondition);

        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            int index = 1;

            if (null != queryErrorCondition.getId()) {
                st.setLong(index++, queryErrorCondition.getId());
            }

            if (null != queryErrorCondition.getEntity()) {
                st.setLong(index++, queryErrorCondition.getEntity());
            }

            if (null != queryErrorCondition.getFixedStatus()) {
                st.setInt(index++, queryErrorCondition.getFixedStatus().getStatus());
            }

            if (null != queryErrorCondition.getStartTime()) {
                st.setLong(index++, queryErrorCondition.getStartTime());
            }

            if (null != queryErrorCondition.getEndTime()) {
                st.setLong(index++, queryErrorCondition.getEndTime());
            }

            st.setLong(index++, queryErrorCondition.getStartPos());
            st.setLong(index, queryErrorCondition.getSize());

            checkTimeout(st);

            List<ErrorStorageEntity> errorStorageEntities = new ArrayList<>();
            try (ResultSet rs = st.executeQuery()) {

                while (rs.next()) {
                    ErrorStorageEntity.Builder builder = ErrorStorageEntity.Builder.anErrorStorageEntity()
                        .withMaintainId(rs.getLong(ErrorDefine.ID))
                        .withId(rs.getLong(ErrorDefine.ID))
                        .withEntity(rs.getLong(ErrorDefine.ENTITY))
                        .withErrors(rs.getString(ErrorDefine.ERRORS))
                        .withFixedStatus(rs.getInt(ErrorDefine.STATUS))
                        .withExecuteTime(rs.getLong(ErrorDefine.EXECUTE_TIME))
                        .withFixedTime(rs.getInt(ErrorDefine.FIXED_TIME));

                    errorStorageEntities.add(builder.build());
                }

                return errorStorageEntities;
            }
        }
    }

    private String buildSQL(QueryErrorCondition condition) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(String.join(",",
            ErrorDefine.ID,
            ErrorDefine.ENTITY,
            ErrorDefine.ERRORS,
            ErrorDefine.EXECUTE_TIME,
            ErrorDefine.FIXED_TIME,
            ErrorDefine.STATUS
            )
        );


        sql.append(" FROM ")
            .append(getTableName())
            .append(" WHERE ");

        boolean isAnd = false;

        if (null != condition.getId()) {
            sql.append(ErrorDefine.ID).append("=").append("?");
            isAnd = true;
        }

        if (null != condition.getEntity()) {
            if (isAnd) {
                sql.append(" AND ");
            }
            sql.append(ErrorDefine.ENTITY).append("=").append("?");
            isAnd = true;
        }

        if (null != condition.getFixedStatus()) {
            if (isAnd) {
                sql.append(" AND ");
            }
            sql.append(ErrorDefine.STATUS).append("=").append("?");
            isAnd = true;
        }

        if (null != condition.getStartTime()) {
            if (isAnd) {
                sql.append(" AND ");
            }
            sql.append(ErrorDefine.EXECUTE_TIME).append(">").append("?");
            isAnd = true;
        }

        if (null != condition.getEndTime()) {
            if (isAnd) {
                sql.append(" AND ");
            }
            sql.append(ErrorDefine.EXECUTE_TIME).append("<").append("?");
        }

        sql.append(" ORDER BY ").append(ErrorDefine.EXECUTE_TIME).append(" DESC ");
        sql.append("LIMIT ").append("?,?");
        return sql.toString();
    }
}
