package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/3/2020 7:07 PM
 * 功能描述:
 * 修改历史:
 */
public class CommonUtil {

    public static void checkId(long id) throws SQLException {
        if (id == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }


    /**
     * fieldId + fieldvalue(unicode) + space + fieldId + fieldvalue(unicode)....n
     */
    public static Set<String> serializeSetFull(StorageStrategyFactory storageStrategyFactory, IEntityValue entityValue) {
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

    /**
     * {
     * "{fieldId}" : fieldValue
     * }
     */
    public static JSONObject serializeToJson(StorageStrategyFactory storageStrategyFactory, IEntityValue values, boolean encodeString) {
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

    // 格式化全文属性为字符串.
    public static String toFullString(Set<String> fullFields) {
        return fullFields.stream().collect(Collectors.joining(" "));
    }

    // 格式化 JSON 属性为字符串.
    public static String toJsonString(JSONObject jsonObject) {
        return JSON.toJSONString(jsonObject);
    }

    // 转换 json 字段为全文搜索字段.
    public static Set<String> convertJsonToFull(JSONObject jsonObject) {
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

    public static StorageEntity selectStorageEntity(Connection conn, String tableName, long id) throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String sql = String.format(SQLConstant.SELECT_FROM_ID_SQL, tableName);
            st = conn.prepareStatement(sql);
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

            return storageEntity;
        } finally {
            if (rs != null) {
                rs.close();
            }

            if (st != null) {
                st.close();
            }
        }
    }
}
