package com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor;

import com.xforceplus.ultraman.oqsengine.common.hash.Time33Hash;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.define.SqlTemplateDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 批量获取KV.
 *
 * @author dongbin
 * @version 0.1 2021/08/03 11:03
 * @since 1.8
 */
public class GetsTaskExecutor
    extends AbstractJdbcTaskExecutor<Collection<String>, Collection<Map.Entry<String, byte[]>>> {

    public GetsTaskExecutor(String tableName,
                            TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public GetsTaskExecutor(String tableName,
                            TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Collection<Map.Entry<String, byte[]>> execute(Collection<String> keys) throws SQLException {
        String sql = String.format(SqlTemplateDefine.SELECTS_TEMPLATE, getTableName(),
            buildQuestionMask(keys.size()), buildQuestionMask(keys.size()));

        try (PreparedStatement ps = getResource().value().prepareStatement(sql)) {

            checkTimeout(ps);

            int pos = 1;
            for (String key : keys) {
                ps.setString(pos++, key);
            }
            for (String key : keys) {
                ps.setLong(pos++, Time33Hash.getInstance().hash(key));
            }

            List<Map.Entry<String, byte[]>> results = new ArrayList<>(keys.size());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(
                        new AbstractMap.SimpleEntry<>(rs.getString(FieldDefine.KEY), rs.getBytes(FieldDefine.VALUE)));
                }

            }

            return results;
        }
    }

    private String buildQuestionMask(int size) {
        return String.join(",", Collections.nCopies(size, "?"));
    }
}