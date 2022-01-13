package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.JsonAttributeMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

/**
 * 创建数据执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 14:41
 * @since 1.8
 */
public class BuildExecutor extends AbstractJdbcTaskExecutor<JsonAttributeMasterStorageEntity[], boolean[]> {

    private static int ENTITY_CLASS_SIZE = 5;

    public static Executor<JsonAttributeMasterStorageEntity[], boolean[]> build(
        String tableName, TransactionResource resource, long timeout) {
        return new BuildExecutor(tableName, resource, timeout);
    }

    public BuildExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public BuildExecutor(String tableName, TransactionResource<Connection> resource, long timeout) {
        super(tableName, resource, timeout);
    }

    @Override
    public boolean[] execute(JsonAttributeMasterStorageEntity[] masterStorageEntities) throws Exception {

        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            checkTimeout(st);

            // 判断是否为单个操作.
            boolean single = masterStorageEntities.length == 1;

            if (single) {

                setParam(masterStorageEntities[0], st);

            } else {

                for (JsonAttributeMasterStorageEntity entity : masterStorageEntities) {

                    setParam(entity, st);

                    st.addBatch();
                }
            }

            return executedUpdate(st, !single);
        }
    }

    private void setParam(JsonAttributeMasterStorageEntity entity, PreparedStatement st) throws SQLException {
        int pos = 1;
        st.setLong(pos++, entity.getId());
        st.setInt(pos++, entity.getEntityClassVersion());
        st.setLong(pos++, entity.getTx());
        st.setLong(pos++, entity.getCommitid());
        st.setInt(pos++, entity.getOp());
        st.setInt(pos++, entity.getVersion());
        st.setLong(pos++, entity.getCreateTime());
        st.setLong(pos++, entity.getUpdateTime());
        st.setBoolean(pos++, false);
        st.setString(pos++, entity.getAttribute());
        st.setInt(pos++, OqsVersion.MAJOR);
        st.setString(pos++, entity.getProfile());
        fullEntityClass(pos, st, entity);
    }

    private int fullEntityClass(int startPos,
                                PreparedStatement st,
                                JsonAttributeMasterStorageEntity masterStorageEntity)
        throws SQLException {
        int pos = startPos;
        for (int i = 0; i < FieldDefine.ENTITYCLASS_LEVEL_LIST.length; i++) {
            if (i < masterStorageEntity.getEntityClasses().length) {
                st.setLong(pos++, masterStorageEntity.getEntityClasses()[i]);
            } else {
                st.setLong(pos++, 0);
            }
        }
        return pos;
    }

    private String buildSQL() {
        StringBuilder buff = new StringBuilder();
        // insert into ${table}
        buff.append("INSERT INTO ").append(getTableName())
            .append(" (").append(String.join(",",
                FieldDefine.ID,
                FieldDefine.ENTITYCLASS_VERSION,
                FieldDefine.TX,
                FieldDefine.COMMITID,
                FieldDefine.OP,
                FieldDefine.VERSION,
                FieldDefine.CREATE_TIME,
                FieldDefine.UPDATE_TIME,
                FieldDefine.DELETED,
                FieldDefine.ATTRIBUTE,
                FieldDefine.OQS_MAJOR,
                FieldDefine.PROFILE)
        );

        for (int i = 0; i < FieldDefine.ENTITYCLASS_LEVEL_LIST.length; i++) {
            buff.append(",")
                .append(FieldDefine.ENTITYCLASS_LEVEL_LIST[i]);
        }

        final int baseColumnSize = 12;

        buff.append(") VALUES (")
            .append(String.join(",", Collections.nCopies(
                baseColumnSize + FieldDefine.ENTITYCLASS_LEVEL_LIST.length, "?")))
            .append(")");
        return buff.toString();
    }
}
