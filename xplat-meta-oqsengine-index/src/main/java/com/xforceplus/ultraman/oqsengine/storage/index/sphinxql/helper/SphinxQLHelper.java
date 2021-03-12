package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.ShortStorageName;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;

import java.util.Arrays;

/**
 * SphinxQL帮助类.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 18:41
 * @since 1.8
 */
public class SphinxQLHelper {

    private static final int[] IGNORE_SYMBOLS = {
        '\'', '\\', '\"', '\n', '\r', '\0', '+', '-', '#', '%', '.', '~', '_', '±', '×', '÷', '=', '≠', '≡', '≌', '≈',
        '<', '>', '≮', '≯', '≤', '≥', '‰', '∞', '∝', '√', '∵', '∴', '∷', '∠', '⌒', '⊙', '○', 'π', '△', '⊥', '∪', '∩',
        '∫', '∑', '°', '′', '″', '℃', '{', '}', '(', ')', '[', ']', '|', '‖', '*', '/', ':', ';', '?', '!', '&', '～',
        '§', '→', '^', '$', '@', '`', '❤', '❥', '︼', '﹄', '﹂', 'ˉ', '︾', '︺', '﹀', '︸', '︶', '︻', '﹃', '﹁',
    };

    static {
        Arrays.sort(IGNORE_SYMBOLS);
    }

    /**
     * 编码处理全文搜索字符.
     *
     * @param value 目标字串.
     * @return 结果.
     */
    public static String encodeFullSearchCharset(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder buff = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (Arrays.binarySearch(IGNORE_SYMBOLS, c) < 0) {
                buff.append(c);
            }
        }

        return buff.toString();
    }

    /**
     * JSON储存字符编码处理.
     * @param value 目标字符.
     * @return 处理结果.
     */
    public static String encodeJsonCharset(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder buff = new StringBuilder();
        for (char c : value.toCharArray()) {
            if ('\'' == c) {
                buff.append('\\');
            }
            buff.append(c);
        }

        return buff.toString();
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
        ShortStorageName shortStorageName = value.shortStorageName();

        buff.append(shortStorageName.getPrefix())
            .append(encodeFullSearchCharset(value.value().toString()));
        if (useGroupName) {
            buff.append(shortStorageName.getNoTypeSuffix()).append("*");
        } else {
            buff.append(shortStorageName.getSuffix());
        }

        return buff.toString();
    }

    /**
     * 构造 sphinxQL 全文索引中的模糊查询语句.
     * 物理名称的62进制,拆分为前6后6.
     * aZl8N0{空格}test{空格}y58M7S
     * 只能处理StorageValue.STRING类型.
     *
     * @param useGroupName 未使用,只为兼容存在.
     * @see StorageType
     */
    public static String buildFullFuzzyQuery(StorageValue value, boolean useGroupName) {
        StringBuilder buff = new StringBuilder();
        ShortStorageName shortStorageName = value.shortStorageName();

        buff.append("(")
            .append(shortStorageName.getPrefix())
            .append(" << *")
            .append(encodeFullSearchCharset(value.value().toString()))
            .append("* << ")
            .append(shortStorageName.getSuffix())
            .append(")");

        return buff.toString();
    }

}
