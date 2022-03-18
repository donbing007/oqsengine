package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.original;

import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.BaseMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 静态对象的删除.
 * 注意: 不会处理任何OQS控制信息.
 *
 * @author dongbin
 * @version 0.1 2022/3/15 15:08
 * @since 1.8
 */
public class OriginalDeleteExecutor extends AbstractOriginalMasterTaskExecutor<BaseMasterStorageEntity[], boolean[]> {

    public OriginalDeleteExecutor(String tableName,
                                  TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public OriginalDeleteExecutor(String tableName,
                                  TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public boolean[] execute(BaseMasterStorageEntity[] entities) throws Exception {

        boolean single = entities.length == 1;

        try (Statement st = getResource().value().createStatement()) {
            checkTimeout(st);

            if (single) {

                boolean[] results = new boolean[] {
                    st.executeUpdate(buildSql(entities[0])) > 0
                };
                setOriginalProcessStatus(entities, results);
                return results;

            } else {

                for (BaseMasterStorageEntity entity : entities) {
                    st.addBatch(buildSql(entity));
                }

                boolean[] results = executedUpdate(st, true);
                setOriginalProcessStatus(entities, results);
                return results;
            }
        }
    }

    private String buildSql(BaseMasterStorageEntity entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ")
            .append(entity.getOriginalTableName())
            .append(" WHERE ")
            .append(FieldDefine.ID).append(" = ").append(entity.getId());
        return sql.toString();
    }

}
