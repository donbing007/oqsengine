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
 * 查询指定数量符合要求的key.
 * 可以指定从头查询还是从尾查询.
 *
 * @author dongbin
 * @version 0.1 2021/07/26 11:15
 * @since 1.8
 */
public class SelectKeysTaskExecutor extends AbstractJdbcTaskExecutor<String, Collection<String>> {

    private static final String SQL_ORDER_ASC = "ASC";
    private static final String SQL_ORDER_DESC = "DESC";

    private String lastKey;
    private int blockSize;
    private boolean asc;

    public SelectKeysTaskExecutor(String tableName,
                                  TransactionResource<Connection> resource) {
        this(tableName, resource, 0);
    }

    public SelectKeysTaskExecutor(String tableName,
                                  TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    public void setLastKey(String lastKey) {
        this.lastKey = lastKey;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
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
        String sql = String.format(
            SqlTemplateDefine.ITERATOR_NO_FIRST_TEMPLATE,
            getTableName(),
            asc ? ">" : "<",
            asc ? SQL_ORDER_ASC : SQL_ORDER_DESC);
        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {
            checkTimeout(ps);

            ps.setString(1, startKey);
            ps.setString(2, this.lastKey);
            ps.setLong(3, this.blockSize);

            return doBuildResult(ps);
        }
    }

    private Collection<String> doFristTime(String startKey) throws SQLException {
        String sql = String.format(
            SqlTemplateDefine.ITERATOR_FIRST_TEMPLATE, getTableName(), asc ? SQL_ORDER_ASC : SQL_ORDER_DESC);
        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {
            checkTimeout(ps);

            ps.setString(1, startKey);
            ps.setLong(2, this.blockSize);

            return doBuildResult(ps);
        }
    }

    private Collection<String> doBuildResult(PreparedStatement ps) throws SQLException {
        List<String> keys = new ArrayList(this.blockSize);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                keys.add(rs.getString(FieldDefine.KEY));
            }
        }

        return keys;
    }
}
