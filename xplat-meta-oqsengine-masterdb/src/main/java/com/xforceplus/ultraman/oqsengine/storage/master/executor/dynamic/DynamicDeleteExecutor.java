package com.xforceplus.ultraman.oqsengine.storage.master.executor.dynamic;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.BaseMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 删除执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 16:03
 * @since 1.8
 */
public class DynamicDeleteExecutor extends AbstractJdbcTaskExecutor<BaseMasterStorageEntity[], boolean[]> {

    public static Executor<BaseMasterStorageEntity[], boolean[]> build(
        String tableName, TransactionResource resource, long timeout) {
        return new DynamicDeleteExecutor(tableName, resource, timeout);
    }

    public DynamicDeleteExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public DynamicDeleteExecutor(String tableName, TransactionResource<Connection> resource, long timeout) {
        super(tableName, resource, timeout);
    }

    @Override
    public boolean[] execute(BaseMasterStorageEntity[] masterStorageEntities) throws Exception {
        // 判断是否为单个操作.
        boolean single = masterStorageEntities.length == 1;

        if (single) {
            BaseMasterStorageEntity entity = masterStorageEntities[0];

            String sql = buildSQL();
            try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
                checkTimeout(st);

                setParam(entity, st);
                return executedUpdate(st, false);
            }

        } else {
            // 批量全部强制删除
            String sql = buildSQL();
            try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
                for (BaseMasterStorageEntity entity : masterStorageEntities) {
                    setParam(entity, st);

                    st.addBatch();
                }
                return executedUpdate(st, true);
            }
        }
    }

    private void setParam(BaseMasterStorageEntity entity, PreparedStatement st) throws SQLException {
        st.setBoolean(1, true);
        st.setLong(2, entity.getUpdateTime());
        st.setLong(3, entity.getTx());
        st.setLong(4, entity.getCommitid());
        st.setInt(5, entity.getOp());
        st.setInt(6, entity.getEntityClassVersion());
        st.setLong(7, entity.getId());
        st.setInt(8, entity.getVersion());
    }

    private String buildSQL() {
        //"update %s set version = version + 1, deleted = ?, time = ?, tx = ?, commitid = ?, op = ? where id = ?;
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append(FieldDefine.VERSION).append(" + 1, ")
            .append(FieldDefine.DELETED).append("=").append("?, ")
            .append(FieldDefine.UPDATE_TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("?, ")
            .append(FieldDefine.ENTITYCLASS_VERSION).append("=").append("? ")
            .append("WHERE ")
            .append(FieldDefine.ID).append("=").append('?');

        return sql.toString();
    }
}
