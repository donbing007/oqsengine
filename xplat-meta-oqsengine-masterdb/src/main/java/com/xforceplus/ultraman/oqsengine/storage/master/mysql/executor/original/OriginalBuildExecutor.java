package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.original;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.MapAttributeMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.JdbcOriginalFieldAgent;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.JdbcOriginalFieldAgentFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Map;

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
        String sql = buildSql();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            checkTimeout(st);

            for (MapAttributeMasterStorageEntity<IEntityField, StorageValue> e : storageEntities) {

                Collection<IEntityField> fields = getEntityClass().fields();
                Map<IEntityField, StorageValue> attributes = e.getAttributes();

                /*
                会处理当前元信息定义的静态字段的所有属性信息.
                 */
                // 当前字段的顺序.从1开始.
                int fieldIndex = 1;
                for (IEntityField field : fields) {

                    StorageValue storageValue = attributes.get(field);
                    JdbcOriginalFieldAgent agent =
                        (JdbcOriginalFieldAgent) JdbcOriginalFieldAgentFactory.getInstance().getAgent(0);
                    agent.write(field, storageValue, new WriteJdbcOriginalSource(fieldIndex++, st));

                }
            }
        }
    }

    private String buildSql() {
        StringBuilder buff = new StringBuilder();
        // INSERT INTO oqs_app_bussiness_profile (id,  语句的前半段.
        buff.append("INSERT INTO ").append(buildOriginalTableName())
            .append(" (").append(FieldDefine.ID).append(",");

        int emptyLen = buff.length();
        for (IEntityField f : getEntityClass().fields()) {
            if (buff.length() > emptyLen) {
                buff.append(",");
            }

            buff.append(f.name());
        }

        // 字段定义的闭合括号
        buff.append(") ");

        // 根据字段数量生成 "?".
        buff.append("VALUES (");

        emptyLen = buff.length();
        for (IEntityField f : getEntityClass().fields()) {
            if (buff.length() > emptyLen) {
                buff.append(", ");
            }

            buff.append("?");
        }

        buff.append(")");

        return buff.toString();
    }
}
