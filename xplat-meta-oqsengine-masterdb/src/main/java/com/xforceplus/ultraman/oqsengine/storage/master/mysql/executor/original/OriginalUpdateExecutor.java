package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.original;

import com.xforceplus.ultraman.oqsengine.common.jdbc.TypesUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.MapAttributeMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.OriginalFieldAgent;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.JdbcOriginalFieldAgent;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.JdbcOriginalFieldAgentFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;

/**
 * 静态对象的更新.
 * 注意: 不会处理OQS控制信息.
 *
 * @author dongbin
 * @version 0.1 2022/3/14 12:05
 * @since 1.8
 */
public class OriginalUpdateExecutor extends
    AbstractOriginalMasterTaskExecutor<MapAttributeMasterStorageEntity<IEntityField, StorageValue>[], boolean[]> {

    public OriginalUpdateExecutor(String tableName,
                                  TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public OriginalUpdateExecutor(String tableName,
                                  TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public boolean[] execute(MapAttributeMasterStorageEntity<IEntityField, StorageValue>[] storageEntities)
        throws Exception {

        boolean single = storageEntities.length == 1;
        try (Statement st = getResource().value().createStatement()) {
            checkTimeout(st);

            if (single) {
                MapAttributeMasterStorageEntity<IEntityField, StorageValue> storageEntity = storageEntities[0];
                String sql = buildSql(storageEntity);

                boolean[] results = new boolean[] {
                    st.executeUpdate(sql) > 0
                };

                setOriginalProcessStatus(storageEntities, results);
                return results;
            } else {

                for (MapAttributeMasterStorageEntity<IEntityField, StorageValue> storageEntity : storageEntities) {
                    String sql = buildSql(storageEntity);
                    st.addBatch(sql);
                }

                boolean[] results = executedUpdate(st, true);
                setOriginalProcessStatus(storageEntities, results);
                return results;
            }
        }
    }

    private String buildSql(MapAttributeMasterStorageEntity<IEntityField, StorageValue> storageEntity)
        throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ")
            .append(storageEntity.getOriginalTableName())
            .append(" SET ");

        Map<IEntityField, StorageValue> attributes = storageEntity.getAttributes();
        JdbcOriginalFieldAgentFactory fieldAgentFactory = JdbcOriginalFieldAgentFactory.getInstance();
        int emptyLen = sql.length();
        Optional<OriginalFieldAgent> agentOp;
        JdbcOriginalFieldAgent agent;
        for (IEntityField field : attributes.keySet()) {
            agentOp = fieldAgentFactory.getAgent(field.config().getJdbcType());
            if (agentOp.isPresent()) {
                agent = (JdbcOriginalFieldAgent) agentOp.get();
                if (sql.length() > emptyLen) {
                    sql.append(", ");
                }

                sql.append(field.name()).append(" = ").append(agent.plainText(field, attributes.get(field)));
            } else {

                Optional<String> typeName = TypesUtils.name(field.config().getJdbcType());
                throw new SQLException(String.format(
                    "Unable to process field %s, unable to find proxy for field. "
                        + "This field declares itself as a primitive type of %s.",
                    field.name(), typeName.orElse("NULL")
                ));

            }
        }

        sql.append(" WHERE ")
            .append(FieldDefine.ID)
            .append(" = ")
            .append(storageEntity.getId());

        return sql.toString();
    }
}
