package com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor;

import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.SqlTemplateDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author dongbin
 * @version 0.1 2021/07/26 11:15
 * @since 1.8
 */
public class ListKeysTaskExecutor extends AbstractJdbcTaskExecutor<String, Collection<String>> {

    private String lastKey;
    private int blockSize;

    public ListKeysTaskExecutor(String tableName,
                                TransactionResource<Connection> resource) {
        this(tableName, resource, 0);
    }

    public ListKeysTaskExecutor(String tableName,
                                TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    public void setLastKey(String lastKey) {
        this.lastKey = lastKey;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    @Override
    public Collection<String> execute(String startKey) throws SQLException {
        if (lastKey == null) {
            return doFristTime(startKey);
        } else {
            return doNoFirstTime(startKey);
        }
    }

    private Collection<String> doNoFirstTime(String startKey) throws SQLException {
        String sql = String.format(SqlTemplateDefine.ITERATOR_NO_FIRST_TEMPLATE, getTableName());
        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {
            checkTimeout(ps);

            ps.setString(1, startKey);
            ps.setString(2, this.lastKey);
            ps.setLong(3, this.blockSize);

            return doBuildResult(ps);
        }
    }

    private Collection<String> doFristTime(String startKey) throws SQLException {
        String sql = String.format(SqlTemplateDefine.ITERATOR_FIRST_TEMPLATE, getTableName());
        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {
            checkTimeout(ps);

            ps.setString(1, startKey);
            ps.setLong(2, this.blockSize);

            return doBuildResult(ps);
        }
    }

    private Collection<String> doBuildResult(PreparedStatement ps) throws SQLException {
        List<String> keys = new ArrayList((int) this.blockSize);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                keys.add(rs.getString(FieldDefine.KEY));
            }
        }

        return keys;
    }
}
