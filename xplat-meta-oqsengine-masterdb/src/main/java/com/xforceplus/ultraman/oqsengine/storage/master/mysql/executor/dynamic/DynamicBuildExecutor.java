package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.serializable.utils.JacksonDefaultMapper;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.AbstractMasterTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.MapAttributeMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * 创建数据执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 14:41
 * @since 1.8
 */
public class DynamicBuildExecutor
    extends AbstractMasterTaskExecutor<MapAttributeMasterStorageEntity<IEntityField, StorageValue>[], boolean[]> {

    public static Executor<MapAttributeMasterStorageEntity<IEntityField, StorageValue>[], boolean[]> build(
        String tableName, TransactionResource resource, long timeout) {
        return new DynamicBuildExecutor(tableName, resource, timeout);
    }

    public DynamicBuildExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public DynamicBuildExecutor(String tableName, TransactionResource<Connection> resource, long timeout) {
        super(tableName, resource, timeout);
    }

    @Override
    public boolean[] execute(MapAttributeMasterStorageEntity<IEntityField, StorageValue>[] masterStorageEntities)
        throws Exception {

        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            checkTimeout(st);

            // 判断是否为单个操作.
            boolean single = masterStorageEntities.length == 1;

            if (single) {

                setParam(masterStorageEntities[0], st);

            } else {

                for (MapAttributeMasterStorageEntity<IEntityField, StorageValue> entity : masterStorageEntities) {

                    setParam(entity, st);

                    st.addBatch();
                }
            }

            boolean[] results = executedUpdate(st, !single);
            setDynamicProcessStatus(masterStorageEntities, results);

            return results;
        }
    }

    private void setParam(MapAttributeMasterStorageEntity<IEntityField, StorageValue> entity, PreparedStatement st)
        throws SQLException {
        int pos = 1;
        /*
                FieldDefine.ID,
                FieldDefine.TX,
                FieldDefine.COMMITID,
                FieldDefine.CREATE_TIME,
                FieldDefine.UPDATE_TIME,
                FieldDefine.ENTITYCLASS_VERSION,
                FieldDefine.OP,
                FieldDefine.VERSION,
                FieldDefine.OQS_MAJOR,
                FieldDefine.DELETED,
                FieldDefine.PROFILE,
                FieldDefine.ATTRIBUTE,
                FieldDefine.ENTITYCLASSL0,
                FieldDefine.ENTITYCLASSL1,
                FieldDefine.ENTITYCLASSL2,
                FieldDefine.ENTITYCLASSL3,
                FieldDefine.ENTITYCLASSL4
         */
        st.setLong(pos++, entity.getId());
        st.setLong(pos++, entity.getTx());
        st.setLong(pos++, entity.getCommitid());
        st.setLong(pos++, entity.getCreateTime());
        st.setLong(pos++, entity.getUpdateTime());
        st.setInt(pos++, entity.getEntityClassVersion());
        st.setInt(pos++, entity.getOp());
        st.setInt(pos++, entity.getVersion());
        st.setInt(pos++, OqsVersion.MAJOR);
        st.setBoolean(pos++, false);
        st.setString(pos++, entity.getProfile());
        st.setString(pos++, toBuildJson(entity.getAttributes()));
        fullEntityClass(pos, st, entity);
    }

    private int fullEntityClass(int startPos,
                                PreparedStatement st,
                                MapAttributeMasterStorageEntity masterStorageEntity)
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
                FieldDefine.TX,
                FieldDefine.COMMITID,
                FieldDefine.CREATE_TIME,
                FieldDefine.UPDATE_TIME,
                FieldDefine.ENTITYCLASS_VERSION,
                FieldDefine.OP,
                FieldDefine.VERSION,
                FieldDefine.OQS_MAJOR,
                FieldDefine.DELETED,
                FieldDefine.PROFILE,
                FieldDefine.ATTRIBUTE));

        for (int i = 0; i < FieldDefine.ENTITYCLASS_LEVEL_LIST.length; i++) {
            buff.append(",")
                .append(FieldDefine.ENTITYCLASS_LEVEL_LIST[i]);
        }

        // 不算上entityclassl0 - 4 的插入字段数量.
        final int baseColumnSize = 12;

        buff.append(") VALUES (")
            .append(String.join(",", Collections.nCopies(
                baseColumnSize + FieldDefine.ENTITYCLASS_LEVEL_LIST.length, "?")))
            .append(")");
        return buff.toString();
    }

    // 构造创建实例属性JSON字符串,名称使用的是属性 F + {id}.
    private String toBuildJson(Map<IEntityField, StorageValue> storageValues) {

        Map<String, Object> values = toPainValues(storageValues);

        try {
            return JacksonDefaultMapper.OBJECT_MAPPER.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
