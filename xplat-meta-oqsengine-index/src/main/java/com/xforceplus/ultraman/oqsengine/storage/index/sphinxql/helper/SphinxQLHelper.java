package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValueFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

/**
 * @author dongbin
 * @version 0.1 2020/2/22 18:41
 * @since 1.8
 */
public class SphinxQLHelper {

    /**
     * 全文搜索字段前辍.
     */
    public static final String FULL_FIELD_PREFIX = "F";

    /**
     * 序列化entity 属性至全文搜索形式.
     * f{field.id}{value | unicode}
     * 如果是字符串,将进行 unicode 编码.
     * @param value 属性值.
     * @return 序列化结果.
     */
//    public static String serializeFull(IValue value, StorageStrategy storageStrategy) {
//        StringBuilder buff = new StringBuilder();
//
//        StorageValue point = storageStrategy.toStorageValue(value);
//
//        while(point != null) {
//            if (buff.length() > 0) {
//                buff.append(" ");
//            }
//            buff.append(FULL_FIELD_PREFIX)
//                .append(point.storageName());
//            if (storageStrategy.storageType() == StorageType.STRING) {
//                buff.append(SphinxQLHelper.unicode((String) point.value()));
//            } else {
//                buff.append(point.value());
//            }
//
//            point = point.next();
//        }
//
//        return buff.toString();
//    }

    /**
     * 序列化SphinxQL的全文搜索字段.
     * 不会处理多值.
     * @param value 目标值.
     * @return 序例化结果.
     */
    public static String encodeFullText(StorageValue value) {
        return FULL_FIELD_PREFIX
            + value.storageName()
            + (value.type() == StorageType.STRING ? SphinxQLHelper.unicode((String) value.value()) : value.value());
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
     * 数字,大小写字母 *号除外都将使用 unicode 的十六进制码表示.
     * @param str 目标字符串.
     * @return 编码结果.
     */
    public static String unicode(String str) {
        StringBuilder buff = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c == 42 || (c >= 65 && c <= 90) || (c >= 97 && c <= 122) || c >= 48 && c <= 57) {

                buff.append(c);

            } else {
                buff.append(Integer.toHexString(c));
            }
        }

        return buff.toString();
    }
}
