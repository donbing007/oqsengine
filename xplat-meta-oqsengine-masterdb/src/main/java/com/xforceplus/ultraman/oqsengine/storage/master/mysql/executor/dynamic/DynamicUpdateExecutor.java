package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic;

import com.xforceplus.ultraman.oqsengine.common.StringUtils;
import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.ValueWithEmpty;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.AbstractMasterTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.MapAttributeMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

/**
 * 更新执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 15:44
 * @since 1.8
 */
public class DynamicUpdateExecutor extends AbstractMasterTaskExecutor<MapAttributeMasterStorageEntity[], boolean[]> {

    public static Executor<MapAttributeMasterStorageEntity[], boolean[]> build(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new DynamicUpdateExecutor(tableName, resource, timeoutMs);
    }

    public DynamicUpdateExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public DynamicUpdateExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public boolean[] execute(MapAttributeMasterStorageEntity[] masterStorageEntities) throws Exception {

        boolean single = masterStorageEntities.length == 1;
        String sql;
        try (Statement st = getResource().value().createStatement()) {

            checkTimeout(st);


            if (single) {
                MapAttributeMasterStorageEntity entity = masterStorageEntities[0];

                sql = buildSQL(
                    entity,
                    buildReplaceFunction(entity.getAttributes()),
                    buildRemoveFuncation(entity.getAttributes()));

                return new boolean[] {
                    st.executeUpdate(sql) > 0
                };

            } else {

                for (MapAttributeMasterStorageEntity entity : masterStorageEntities) {

                    sql = buildSQL(
                        entity,
                        buildReplaceFunction(entity.getAttributes()),
                        buildRemoveFuncation(entity.getAttributes()));

                    st.addBatch(sql);
                }

                return executedUpdate(st, true);
            }
        }
    }

    private String buildRemoveFuncation(Map<String, Object> attributes) {

        StringBuilder buff = new StringBuilder();

        Object value;
        for (String key : attributes.keySet()) {
            value = attributes.get(key);

            if (!ValueWithEmpty.EMPTY_VALUE.getClass().isInstance(value)) {
                continue;
            }

            buff.append("\"$.").append(key).append("\",");
        }

        if (buff.length() > 0) {
            buff.insert(0, String.join("", "JSON_REMOVE(", FieldDefine.ATTRIBUTE, ", "));
            // 删除尾部多余的逗号
            buff.deleteCharAt(buff.length() - 1);

            buff.append(')');

            return buff.toString();

        } else {

            return "";

        }
    }

    private String buildReplaceFunction(Map<String, Object> attributes) {

        StringBuilder buff = new StringBuilder();

        Object value;
        for (String key : attributes.keySet()) {
            value = attributes.get(key);

            if (ValueWithEmpty.EMPTY_VALUE.getClass().isInstance(value)) {
                continue;
            }

            buff.append("\"$.").append(key).append("\", ");

            if (String.class.isInstance(value)) {
                buff.append("\"")
                    .append(StringUtils.encodeEscapeCharacters(value.toString()))
                    .append("\"");
            } else {
                buff.append(value);
            }
            buff.append(',');
        }

        if (buff.length() > 0) {
            buff.insert(0, String.join("", "JSON_SET(", FieldDefine.ATTRIBUTE, ", "));
            // 删除尾部多余的逗号
            buff.deleteCharAt(buff.length() - 1);

            buff.append(')');
            return buff.toString();
        } else {

            return "";
        }
    }

    private String buildSQL(MapAttributeMasterStorageEntity entity, String replaceSegment, String removeSegment) {
        //"update %s set version = version + 1, updatetime = ?, tx = ?,
        // commitid = ?, op = ?, attribute = ?, attribute = ? where id = ?;

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append(FieldDefine.VERSION).append(" + 1, ")
            .append(FieldDefine.UPDATE_TIME).append("=").append(entity.getUpdateTime()).append(", ")
            .append(FieldDefine.TX).append("=").append(entity.getTx()).append(", ")
            .append(FieldDefine.COMMITID).append("=").append(entity.getCommitid()).append(", ")
            .append(FieldDefine.OP).append("=").append(entity.getOp()).append(", ")
            .append(FieldDefine.OQS_MAJOR).append("=").append(entity.getOqsMajor()).append(", ")
            .append(FieldDefine.ENTITYCLASS_VERSION).append("=").append(entity.getEntityClassVersion());
        if (!replaceSegment.isEmpty()) {
            sql.append(", ");
            sql.append(FieldDefine.ATTRIBUTE).append("=").append(replaceSegment);
        }
        if (!removeSegment.isEmpty()) {
            sql.append(", ");
            sql.append(FieldDefine.ATTRIBUTE).append("=").append(removeSegment);
        }
        sql.append(" WHERE ")
            .append(FieldDefine.ID).append("=").append(entity.getId());
        return sql.toString();
    }
}
