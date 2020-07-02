package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    /**
     * 将 Map 序列化成 json 字符串.
     * 只关注处理 String 和非字符串,并且不会级联处理子对象.
     *
     * @param data 数据哈希.
     * @return json.
     */
    public static String serializableJson(Map<String, Object> data) {
        StringBuilder buff = new StringBuilder();
        buff.append("{");

        final int emptyLen = buff.length();
        for (AbstractMap.Entry<String, Object> entry : data.entrySet()) {
            if (buff.length() > emptyLen) {
                buff.append(",");
            }
            buff.append("\"").append(entry.getKey()).append("\"");
            buff.append(":");
            if (String.class.isInstance(entry.getValue())) {
                buff.append("\"").append(entry.getValue().toString()).append("\"");
            } else {
                buff.append(entry.getValue().toString());
            }
        }

        buff.append("}");

        return buff.toString();
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
        if ("{}".equals(json)) {
            return Collections.emptyMap();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        StringBuilder buff = new StringBuilder();
        String key = null;
        String value;
        boolean number;
        for (char c : json.toCharArray()) {
            // 首字符忽略.
            if ('{' == c) {

                continue;

            } else if (':' == c) {

                // 删除双引号
                buff.deleteCharAt(0);
                buff.deleteCharAt(buff.length() - 1);
                key = buff.toString();
                buff.delete(0, buff.length());

            } else if (',' == c || '}' == c) {

                number = (buff.indexOf("\"") != 0);

                if (number) {
                    // 长整形
                    value = buff.toString();
                    data.put(key, Long.valueOf(value));
                } else {
                    buff.deleteCharAt(0);
                    buff.deleteCharAt(buff.length() - 1);
                    value = buff.toString();
                    // 字符串
                    data.put(key, value);
                }

                buff.delete(0, buff.length());

            } else {

                buff.append(c);

            }
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
        buff.append("(ZONESPAN:").append(ATTRIBUTE_FULL_FIELD_PREFIX).append(value.groupStorageName()).append(" ");

        buff.append("\"*");
        if (StorageType.STRING == value.type()) {
            buff.append(encodeSpecialCharset(value.value().toString()));
        } else {
            buff.append(value.value().toString());
        }
        buff.append('*');
        buff.append("\")");
        return buff.toString();
    }
}
