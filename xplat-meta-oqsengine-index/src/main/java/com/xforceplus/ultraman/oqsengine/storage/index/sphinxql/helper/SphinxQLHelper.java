package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;

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

        return ATTRIBUTE_FULL_FIELD_PREFIX
            + (useGroupName ? (value.groupStorageName() + SqlKeywordDefine.EVERY_THING) : value.storageName())
            + " "
            + (value.type() == StorageType.STRING ? unicode((String) value.value()) : value.value());
    }

    /**
     * 构造查询字符串.
     * 目标为 Fxxx xxx xxxx xxx 以空格分隔的字符串.
     * 结果为 "xxx" << "xxxx" << "xxx"
     *
     * @param value 目标查询值.预期已经为被编码过的全文字段.
     * @param fuzzy 模糊匹配.
     * @return
     */
    public static String encodeQueryFullText(String value, boolean fuzzy) {
        StringBuilder buff = new StringBuilder();
        // 是否已经有<<标识了.
        boolean lastHave = false;
        for (char c : value.toCharArray()) {
            if (c == ' ') {
                if (!lastHave) {

                    if (fuzzy) {
                        buff.append(SqlKeywordDefine.EVERY_THING);
                    }

                    buff.append(" << ");

                    if (fuzzy) {
                        buff.append(SqlKeywordDefine.EVERY_THING);
                    }

                    lastHave = true;
                }
            } else {
                buff.append(c);
                lastHave = false;
            }
        }
        if (fuzzy) {
            buff.append(SqlKeywordDefine.EVERY_THING);
        }

        return buff.toString();
    }

    /**
     * !    "    $    '    (    )    -    /    <    @    \    ^    |    ~ 会被替换成分别为 unicode 码.
     * 为了在 sphinxQL中使用这些字符.
     */
    public static String encodeString(String source) {
        StringBuilder buff = new StringBuilder();
        for (char c : source.toCharArray()) {
            switch (c) {
                case '\"':
                case '\'':
                case '\\':
                case '!':
                case '$':
                case '(':
                case ')':
                case '-':
                case '/':
                case '<':
                case '@':
                case '^':
                case '|':
                case '~':
                case '?':
                case '*':
                    buff.append(doUnicode(c)).append(" ");
                    break;
                default:
                    buff.append(c);
            }
        }

        return buff.toString();
    }

    /**
     * 数字,大小写字母除外都将使用 unicode 的十六进制码表示.
     *
     * @param str 目标字符串.
     * @return 编码结果.
     */
    public static String unicode(String str) {
        StringBuilder buff = new StringBuilder();
        boolean lastUnicode = false;
        for (char c : str.toCharArray()) {
            if (c == ' ' || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c >= '0' && c <= '9') {

                if (lastUnicode) {
                    buff.append(' ');
                }

                buff.append(c);

                lastUnicode = false;

            } else {
                // 隔开关键字
                if (buff.length() > 0) {
                    buff.append(' ');
                }

                buff.append(doUnicode(c));

                lastUnicode = true;
            }
        }

        return buff.toString();
    }

    private static String doUnicode(char c) {
        return Integer.toHexString(c);
    }

    public static void main(String[] args) {
        String data = "2d100";
        System.out.println(encodeFullText(new StringStorageValue("1S", "2d100", false)));
    }
}
