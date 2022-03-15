package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.original;

import com.xforceplus.ultraman.oqsengine.common.map.MapUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.MapAttributeMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.JdbcOriginalFieldAgent;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.JdbcOriginalFieldAgentFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 静态对象的实例查询.
 *
 * @author dongbin
 * @version 0.1 2022/2/24 11:16
 * @since 1.8
 */
public class OriginalQueryExecutor extends
    AbstractOriginalMasterTaskExecutor<long[], Collection<MapAttributeMasterStorageEntity<IEntityField, StorageValue>>> {

    private boolean noDetail;

    public OriginalQueryExecutor(String tableName, TransactionResource<Connection> resource, boolean noDetail) {
        this(tableName, resource, noDetail, 0);
    }

    public OriginalQueryExecutor(
        String tableName, TransactionResource<Connection> resource, boolean noDetail, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public Collection<MapAttributeMasterStorageEntity<IEntityField, StorageValue>> execute(long[] ids) throws Exception {
        String sql = buildSql(ids.length);
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {

            final boolean single = ids.length == 1;
            if (single) {
                st.setLong(1, ids[0]);
            } else {

                for (int i = 0; i < ids.length; i++) {
                    // +1 因为jdbc setXXX序号从1开始.
                    st.setLong(i + 1, ids[i]);
                }

            }
            st.setBoolean(2, false);

            checkTimeout(st);

            Collection<MapAttributeMasterStorageEntity<IEntityField, StorageValue>> results = new ArrayList(ids.length);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    MapAttributeMasterStorageEntity<IEntityField, StorageValue> entity =
                        new MapAttributeMasterStorageEntity();

                    entity.setId(rs.getLong(FieldDefine.ID));
                    entity.setVersion(rs.getInt(FieldDefine.VERSION));
                    entity.setCreateTime(rs.getLong(FieldDefine.CREATE_TIME));
                    entity.setUpdateTime(rs.getLong(FieldDefine.UPDATE_TIME));
                    entity.setOp(rs.getInt(FieldDefine.OP));
                    entity.setProfile(rs.getString(FieldDefine.PROFILE));

                    entity.setEntityClassVersion(rs.getInt(FieldDefine.ENTITYCLASS_VERSION));
                    // 静态不支持继承.
                    entity.setEntityClasses(new long[] { rs.getLong(FieldDefine.ENTITYCLASS_LEVEL_0)});

                    Collection<IEntityField> fields = getEntityClass().fields();
                    Map<IEntityField, StorageValue> values = new HashMap(MapUtils.calculateInitSize(fields.size()));

                    ResultSetMetaData metadata = rs.getMetaData();
                    for (IEntityField f : fields) {
                        int jdbcType = findJdbcType(metadata, f.name());
                        if (Types.NULL == jdbcType) {
                            continue;
                        } else {
                            JdbcOriginalFieldAgent agent =
                                (JdbcOriginalFieldAgent) JdbcOriginalFieldAgentFactory.getInstance().getAgent(jdbcType);
                            int number = rs.findColumn(f.name());
                            StorageValue storageValue = agent.read(f, new ReadJdbcOriginalSource(number, rs));
                            if (!storageValue.isEmpty()) {
                                values.put(f, storageValue);
                            }
                        }
                    }

                    results.add(entity);
                }
            }

            return results;
        }
    }

    private String buildSql(int len) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
            .append("c.").append(FieldDefine.ID).append(", ")
            .append("c.").append(FieldDefine.ENTITYCLASS_LEVEL_0).append(", ")
            .append("c.").append(FieldDefine.ENTITYCLASS_VERSION).append(", ")
            .append("c.").append(FieldDefine.VERSION).append(", ")
            .append("c.").append(FieldDefine.CREATE_TIME).append(", ")
            .append("c.").append(FieldDefine.UPDATE_TIME).append(", ")
            .append("c.").append(FieldDefine.OP).append(", ")
            .append("c.").append(FieldDefine.PROFILE);

        if (!this.noDetail) {
            sql.append(", ");
            int emptyLen = sql.length();
            for (IEntityField f : getEntityClass().fields()) {
                if (sql.length() > emptyLen) {
                    sql.append(", ");
                }
                sql.append("b.").append(f.name());
            }
        }

        sql.append(" FROM ")
            .append(getTableName()).append(" c")
            .append(" INNER JOIN ")
            .append(buildOriginalTableName()).append(" b")
            .append(" ON ")
            .append("c.").append(FieldDefine.ID).append(" = ").append("b.").append(FieldDefine.ID);

        sql.append(" WHERE ");
        sql.append("c.").append(FieldDefine.ID);
        // 单例查询.
        final int onlyOne = 1;
        if (len > onlyOne) {
            int emptyLen = sql.length();
            sql.append(" IN (");
            for (int i = 0; i < len; i++) {
                if (sql.length() != emptyLen) {
                    sql.append(", ");
                }
                sql.append("?");
            }
            sql.append(")");
        } else {
            sql.append(" = ").append('?');
        }

        sql.append(" AND ")
            .append("c.").append(FieldDefine.DELETED).append(" = ").append('?');

        return sql.toString();
    }
}
