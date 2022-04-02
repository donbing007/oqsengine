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
 * 静态对象创建.
 * 注意: 不会处理OQS的控制信息.
 *
 * @author dongbin
 * @version 0.1 2022/3/11 09:42
 * @since 1.8
 */
public class OriginalBuildExecutor extends
    AbstractOriginalMasterTaskExecutor<MapAttributeMasterStorageEntity<IEntityField, StorageValue>[], boolean[]> {

    public OriginalBuildExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public OriginalBuildExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public boolean[] execute(MapAttributeMasterStorageEntity<IEntityField, StorageValue>[] storageEntities)
        throws Exception {
        /*
        构造SQL,和字段的填需要保证同样的迭代顺序.
        这里依赖IEntityClass.fields() 方法的迭代顺序,多次迭代需要是顺序一致的.
         */
        try (Statement st = getResource().value().createStatement()) {
            checkTimeout(st);

            for (MapAttributeMasterStorageEntity<IEntityField, StorageValue> e : storageEntities) {

                String sql = buildSql(e);

                st.addBatch(sql);
            }

            boolean[] results = this.executedUpdate(st, true);
            setOriginalProcessStatus(storageEntities, results);
            return results;
        }
    }

    private String buildSql(MapAttributeMasterStorageEntity<IEntityField, StorageValue> entity) throws Exception {
        StringBuilder buff = new StringBuilder();
        // INSERT INTO oqs_app_bussiness_profile (id,  语句的前半段.
        buff.append("INSERT INTO ").append(entity.getOriginalTableName())
            .append(" (").append(FieldDefine.ID).append(",");

        int emptyLen = buff.length();
        Map<IEntityField, StorageValue> attributes = entity.getAttributes();
        for (IEntityField f : attributes.keySet()) {
            if (buff.length() > emptyLen) {
                buff.append(", ");
            }

            buff.append(f.name());
        }

        // 字段定义的闭合括号
        buff.append(") ");

        buff.append("VALUES (");
        buff.append(entity.getId()).append(", ");

        emptyLen = buff.length();

        for (IEntityField field : attributes.keySet()) {
            if (buff.length() > emptyLen) {
                buff.append(", ");
            }

            Optional<OriginalFieldAgent> agentOp =
                JdbcOriginalFieldAgentFactory.getInstance().getAgent(field.config().getJdbcType());
            if (agentOp.isPresent()) {
                JdbcOriginalFieldAgent agent = (JdbcOriginalFieldAgent) agentOp.get();
                String plainText = agent.plainText(field, attributes.get(field));

                buff.append(plainText);
            } else {

                Optional<String> typeName = TypesUtils.name(field.config().getJdbcType());
                throw new SQLException(String.format(
                    "Unable to process field %s, unable to find proxy for field. "
                        + "This field declares itself as a primitive type of %s(%d).",
                    field.name(), typeName.orElse("NULL"), field.config().getJdbcType()));
            }

        }

        buff.append(")");

        return buff.toString();
    }
}
