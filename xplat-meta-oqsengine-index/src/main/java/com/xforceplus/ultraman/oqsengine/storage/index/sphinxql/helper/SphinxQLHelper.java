package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;

/**
 * @author dongbin
 * @version 0.1 2020/2/22 18:41
 * @since 1.8
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
     * 序列化SphinxQL的全文搜索字段.
     * 不会处理多值.
     *
     * @param value 目标值.
     * @return 序例化结果.
     */
    public static String encodeFullText(StorageValue value) {
        return encodeFullText(value, false);
    }

    public static String encodeFullText(StorageValue value, boolean useGroupName) {
        Object targetValue;
        if (StorageType.STRING == value.type()) {
            targetValue = encodeString((String) value.value());
        } else {
            targetValue = value.value();
        }

        return ATTRIBUTE_FULL_FIELD_PREFIX
            + (useGroupName ? (value.groupStorageName() + SqlKeywordDefine.EVERY_THING) : value.storageName())
            + (value.type() == StorageType.STRING ? unicode((String) targetValue) : targetValue);
    }

    /**
     * " ' \ 会被替换成分别为 ^, ` 和/.
     */
    public static String encodeString(String source) {
        StringBuilder buff = new StringBuilder();
        for (char c : source.toCharArray()) {
            switch (c) {
                case '\"': {
                    buff.append('^');
                    break;
                }
                case '\'': {
                    buff.append('`');
                    break;
                }
                case '\\': {
                    buff.append('/');
                    break;
                }
                default:
                    buff.append(c);
            }
        }

        return buff.toString();
    }

    /**
     * 数字,大小写字母 *号除外都将使用 unicode 的十六进制码表示.
     *
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
