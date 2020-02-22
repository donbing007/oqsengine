package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.helper.StorageTypeHelper;

/**
 * @author dongbin
 * @version 0.1 2020/2/22 18:41
 * @since 1.8
 */
public class SphinxQLHelper {

    public static final String FULL_FIELD_PREFIX = "F";

    /**
     * 序列化entity 属性至全文搜索形式.
     * f{field.id}{value | unicode}
     * 如果是字符串,将进行 unicode 编码.
     * @param value 属性值.
     * @return 序列化结果.
     */
    public static String serializeFull(IValue value) {
        StringBuilder buff = new StringBuilder();
        buff.append(FULL_FIELD_PREFIX);
        buff.append(value.getField().id());
        StorageType current = StorageTypeHelper.findStorageType(value.getField().type());
        if (current == StorageType.STRING) {
            buff.append(SphinxQLHelper.unicode(value.valueToString()));
        } else {
            buff.append(value.valueToLong());
        }
        return buff.toString();
    }

    /**
     * !    "    $    '    (    )    -    /    <    @    \    ^    |    ~ need escaping.
     */
    public static String escapeString(String source) {
        StringBuilder buff = new StringBuilder();
        for (char c : source.toCharArray()) {
            if (c == '!'
                || c == '"'
                || c == '$'
                || c == '\''
                || c == '('
                || c == ')'
                || c == '-'
                || c == '/'
                || c == '<'
                || c == '@'
                || c == '\\'
                || c == '^'
                || c == '|'
                || c == '~'
            ) {
                buff.append('\\');
            }
            buff.append(c);
        }

        return buff.toString();
    }

    /**
     * 数字,大小写字母除外都将使用 unicode 的十六进制码表示.
     * @param str 目标字符串.
     * @return 编码结果.
     */
    public static String unicode(String str) {
        StringBuilder buff = new StringBuilder();
        for (char c : str.toCharArray()) {
            if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122) || c >= 48 && c <= 57) {

                buff.append(c);

            } else {
                buff.append(Integer.toHexString(c));
            }
        }

        return buff.toString();
    }
}
