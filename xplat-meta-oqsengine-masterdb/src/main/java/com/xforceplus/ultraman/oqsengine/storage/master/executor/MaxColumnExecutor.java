package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * desc :
 * name : MaxCommitIdExecutor
 *
 * @author : xujia
 * date : 2020/12/11
 * @since : 1.8
 */
public class MaxColumnExecutor extends AbstractMasterExecutor<String, Optional<Long>> {

    public static Executor<String, Optional<Long>> build(
            String tableName, TransactionResource resource, long timeoutMs) {
        return new MaxColumnExecutor(tableName, resource, timeoutMs);
    }

    public MaxColumnExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Optional<Long> execute(String columnName) throws SQLException {
        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setString(1, columnName);

            checkTimeout(st);

            st.executeQuery();

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong(1));
                }
            }
        }

        return Optional.empty();
    }

    private String buildSQL() {
        //"select MAX(?) from %s";
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT MAX(?) FROM ").append(getTableName());
        return sql.toString();
    }
}
