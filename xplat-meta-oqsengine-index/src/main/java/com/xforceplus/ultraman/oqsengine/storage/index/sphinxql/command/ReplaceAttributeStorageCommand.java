package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant.WRITER_SQL;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 5:00 PM
 * 功能描述:
 * 修改历史:
 */
public class ReplaceAttributeStorageCommand implements StorageCommand {

    final Logger logger = LoggerFactory.getLogger(ReplaceAttributeStorageCommand.class);

    private StorageStrategyFactory storageStrategyFactory;

    private String indexTableName;

    private String buildSql;

    private String replaceSql;

    public ReplaceAttributeStorageCommand(StorageStrategyFactory storageStrategyFactory, String indexTableName){
        this.storageStrategyFactory = storageStrategyFactory;
        this.indexTableName = indexTableName;

        buildSql =
                String.format(WRITER_SQL,
                        "insert", indexTableName,
                        FieldDefine.ID, FieldDefine.ENTITY, FieldDefine.PREF, FieldDefine.CREF,
                        FieldDefine.JSON_FIELDS, FieldDefine.FULL_FIELDS);
        replaceSql =
                String.format(WRITER_SQL,
                        "replace", indexTableName,
                        FieldDefine.ID, FieldDefine.ENTITY, FieldDefine.PREF, FieldDefine.CREF,
                        FieldDefine.JSON_FIELDS, FieldDefine.FULL_FIELDS);
    }

    @Override
    public OpTypeEnum opType() {
        return OpTypeEnum.REPLACE_ATTRIBUTE;
    }

    @Override
    public Object execute(TransactionResource resource, Object data) throws SQLException {
        IEntityValue attribute = (IEntityValue) data;

        long dataId = attribute.id();
        Optional<StorageEntity> oldStorageEntityOptional = doSelectStorageEntity(resource, dataId);
        if (oldStorageEntityOptional.isPresent()) {

            StorageEntity storageEntity = oldStorageEntityOptional.get();

            /**
             * 把新的属性插入旧属性集中替换已有,或新增.
             */
            JSONObject completeJson = storageEntity.getJsonFields();
            JSONObject modifiedJson = serializeToJson(attribute, true);
            for (String key : modifiedJson.keySet()) {
                completeJson.put(key, modifiedJson.get(key));
            }

            //处理 fulltext
            Set<String> completeFull = convertJsonToFull(completeJson);
            storageEntity.setJsonFields(completeJson);
            storageEntity.setFullFields(completeFull);

            doBuildReplaceStorageEntity(resource, storageEntity, true);

        } else {

            throw new SQLException(
                    String.format("Attempt to update a property on a data that does not exist.[%d]", dataId)
            );

        }
        return null;
    }

    /**
     * {
     * "{fieldId}" : fieldValue
     * }
     */
    private JSONObject serializeToJson(IEntityValue values, boolean encodeString) {
        Map<String, Object> data = new HashMap<>(values.values().size());
        values.values().stream().forEach(v -> {
            StorageValue storageValue = storageStrategyFactory.getStrategy(v.getField().type()).toStorageValue(v);

            while (storageValue != null) {
                if (storageValue.type() == StorageType.STRING) {
                    data.put(storageValue.storageName(),
                            encodeString ? SphinxQLHelper.encodeString((String) storageValue.value()) : storageValue.value());
                } else {
                    data.put(storageValue.storageName(), storageValue.value());
                }
                storageValue = storageValue.next();
            }
        });

        return new JSONObject(data);
    }

    /**
     * fieldId + fieldvalue(unicode) + space + fieldId + fieldvalue(unicode)....n
     */
    private Set<String> serializeSetFull(IEntityValue entityValue) {
        Set<String> fullSet = new HashSet<>();
        entityValue.values().stream().forEach(v -> {

            StorageValue storageValue = storageStrategyFactory.getStrategy(v.getField().type()).toStorageValue(v);
            while (storageValue != null) {
                fullSet.add(SphinxQLHelper.encodeFullText(storageValue));
                storageValue = storageValue.next();
            }
        });

        return fullSet;
    }

    // 转换 json 字段为全文搜索字段.
    private Set<String> convertJsonToFull(JSONObject jsonObject) {
        Set<String> fullfileds = new HashSet<>();
        Object value;
        StorageValue storageValue = null;
        for (String key : jsonObject.keySet()) {
            value = jsonObject.get(key);

            if (Integer.class.isInstance(value)) {

                storageValue = new LongStorageValue(key, ((Integer) value).longValue(), false);


            } else if (Long.class.isInstance(value)) {

                storageValue = new LongStorageValue(key, (Long) value, false);

            } else {

                storageValue = new StringStorageValue(key, (String) value, false);
            }

            fullfileds.add(SphinxQLHelper.encodeFullText(storageValue));
        }

        return fullfileds;
    }

    // 查询原始数据.
    private Optional<StorageEntity> doSelectStorageEntity(TransactionResource resource, long id) throws SQLException {
            PreparedStatement st = null;
            ResultSet rs = null;
            try {
                String sql = String.format(SQLConstant.SELECT_FROM_ID_SQL, indexTableName);
                st = ((Connection) resource.value()).prepareStatement(sql);
                st.setLong(1, id);

                rs = st.executeQuery();
                StorageEntity storageEntity = null;
                if (rs.next()) {
                    storageEntity = new StorageEntity(
                            id,
                            rs.getLong(FieldDefine.ENTITY),
                            rs.getLong(FieldDefine.PREF),
                            rs.getLong(FieldDefine.CREF),
                            JSON.parseObject(rs.getString(FieldDefine.JSON_FIELDS)),
                            null
                    );
                }

                return Optional.ofNullable(storageEntity);
            } finally {
                if (rs != null) {
                    rs.close();
                }

                if (st != null) {
                    st.close();
                }
            }
    }

    // 更新原始数据.
    private boolean doBuildReplaceStorageEntity(TransactionResource resource, StorageEntity storageEntity, boolean replacement) throws SQLException {

        final String sql = String.format(replacement ? replaceSql : buildSql, indexTableName);

        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);

        // id, entity, pref, cref, jsonfileds, fullfileds
        st.setLong(1, storageEntity.getId()); // id
        st.setLong(2, storageEntity.getEntity()); // entity
        st.setLong(3, storageEntity.getPref()); // pref
        st.setLong(4, storageEntity.getCref()); // cref
        // attribute
        st.setString(5, toJsonString(storageEntity.getJsonFields()));
        // full
        st.setString(6, toFullString(storageEntity.getFullFields()));

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        int size = st.executeUpdate();

        try {
            // 成功只应该有一条语句影响
            final int onlyOne = 1;
            return size == onlyOne;

        } finally {
            st.close();
        }
    }

    private void checkId(long id) throws SQLException {
        if (id == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

    // 格式化全文属性为字符串.
    private String toFullString(Set<String> fullFields) {
        return fullFields.stream().collect(Collectors.joining(" "));
    }

    // 格式化 JSON 属性为字符串.
    private String toJsonString(JSONObject jsonObject) {
        return JSON.toJSONString(jsonObject);
    }
}
