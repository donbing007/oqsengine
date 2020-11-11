package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dongbin
 * @version 0.1 2020/2/22 18:41
 * ` * @since 1.8
 * `
 */
public class SphinxQLHelper {

    /**
     * 全文搜索字段前辍.
     */
    public static final String ATTRIBUTE_FULL_FIELD_PREFIX = "F";

    /**
     * 表示系统字段的全文字段前辍.
     */
    public static final String SYSTEM_FULL_FIELD_PREFIX = "S";

    /**
     * 表示所有字段的全文查询字串.
     */
    public static final String ALL_DATA_FULL_TEXT = SYSTEM_FULL_FIELD_PREFIX + "g";

    /**
     * 处理以下字段.
     * !    "    $    '    (    )    -    /    <    @    \    ^    |    ~ 空格 *
     * 使用'\'转义.
     *
     * @param value 目标字串.
     * @return 结果.
     */
    public static String encodeSpecialCharset(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder buff = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '!':
                case '$':
                case '\'':
                case '(':
                case ')':
                case '-':
                case '/':
                case '<':
                case '@':
                case '\\':
                case '^':
                case '|':
                case '~':
                case '*':
                case '\"': {
                    // 半角和全角差距为 65248.
                    buff.append((char) (c + 65248));
                    break;
                }
                default: {
                    if (c == ' ') {
                        // 全角空格.
                        buff.append((char) 12288);
                    } else {
                        buff.append(c);
                    }
                }

            }
        }

        return buff.toString();
    }

    /**
     * 处理成全文索引字符串.
     */
    public static String serializeStorageValueFull(StorageValue value) {
        StringBuilder buff = new StringBuilder();
        if (value.type() == StorageType.STRING) {

            buff.append("<").append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.groupStorageName()).append(">");
            buff.append(SphinxQLHelper.encodeSpecialCharset(value.value().toString()));
            buff.append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.storageName());
            buff.append("</").append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.groupStorageName()).append(">");

        } else {

            buff.append(value.value().toString());
            buff.append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.storageName());
        }
        return buff.toString();
    }

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * 将 Map 序列化成 json 字符串.
     * 只关注处理 String 和非字符串,并且不会级联处理子对象.
     *
     * @param data 数据哈希.
     * @return json.
     */
    public static String serializableJson(Map<String, Object> data) {
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将 serializableJson 的结果还原成 Map 表示.
     *
     * @param json json 字符串.
     * @return 数据.
     */
    public static Map<String, Object> deserializeJson(String json) {
        if (!json.startsWith("{")) {
            throw new IllegalStateException("Wrong JSON format.");
        }
        Map<String, Object> data;
        if ("{}".equals(json)) {
            data = new HashMap<>(1, 1.0F);
            return data;
        }

        try {
            data = mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return data;
    }

    /**
     * 构造 sphinxQL 全文索引中精确查询语句.
     * {value}F{field name}
     *
     * @param value 目标字段.
     * @return 结果.
     */
    public static String buildFullPreciseQuery(StorageValue value, boolean useGroupName) {
        StringBuilder buff = new StringBuilder();

        buff.append('\"');
        if (StorageType.STRING == value.type()) {
            buff.append(encodeSpecialCharset(value.value().toString()));
        } else {
            buff.append(value.value().toString());
        }
        buff.append(ATTRIBUTE_FULL_FIELD_PREFIX);
        if (useGroupName) {
            buff.append(value.groupStorageName()).append("*");
        } else {
            buff.append(value.storageName());
        }
        buff.append("\"");
        return buff.toString();
    }

    /**
     * 构造 sphinxQL 全文索引中的模糊查询语句.
     * (ZONESPAN:{字段组名}F{字段组名} *value*)
     *
     * @param value
     * @return
     */
    public static String buildFullFuzzyQuery(StorageValue value, boolean useGroupName) {
        StringBuilder buff = new StringBuilder();
        buff.append("\"*");
        if (StorageType.STRING == value.type()) {
            buff.append(encodeSpecialCharset(value.value().toString()));
        } else {
            buff.append(value.value().toString());
        }
        buff.append("*");

        // TODO: 这里的暂时换成了非精确的模糊搜索方式,原有的ZONESPAN对于性能消耗很大.
//        buff.append(ATTRIBUTE_FULL_FIELD_PREFIX);
//        if (useGroupName) {
//            buff.append(value.groupStorageName()).append("*");
//        } else {
//            buff.append(value.storageName());
//        }
        buff.append("\"");
        return buff.toString();
    }
}
