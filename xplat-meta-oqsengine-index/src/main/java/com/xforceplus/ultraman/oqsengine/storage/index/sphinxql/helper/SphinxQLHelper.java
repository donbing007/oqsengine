package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.common.string.StringUtils;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.ShortStorageName;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;

import java.util.Arrays;
import java.util.Iterator;

/**
 * SphinxQL帮助类.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 18:41
 * @since 1.8
 */
public class SphinxQLHelper {

    /**
     * 半角空格不可过滤,只有全角空格需要过滤.
     */
    protected static final int[] IGNORE_SYMBOLS = {
        '\'', '\\', '\"', '+', '-', '#', '%', '.', '~', '_', '±', '×', '÷', '=', '≠', '≡', '≌', '≈',
        '<', '>', '≮', '≯', '≤', '≥', '‰', '∞', '∝', '√', '∵', '∴', '∷', '∠', '⌒', '⊙', '○', 'π', '△', '⊥', '∪', '∩',
        '∫', '∑', '°', '′', '″', '℃', '{', '}', '(', ')', '[', ']', '|', '‖', '*', '/', ':', ';', '?', '!', '&', '～',
        '§', '→', '^', '$', '@', '`', '❤', '❥', '︼', '﹄', '﹂', 'ˉ', '︾', '︺', '﹀', '︸', '︶', '︻', '﹃', '﹁',
        // 全角
        '！', '＂', '＃', '＄', '％', '＆', '＇', '（', '）', '＊', '＋', '－', '．', '／', '：', '；', '＜', '＝', '＞', '？',
        '＠', '［', '＼', '］', '＾', '＿', '｀', '｛', '｜', '｝', '～', '　',
    };

    static {
        Arrays.sort(IGNORE_SYMBOLS);
    }

    /**
     * 过滤所有不合式的符号.
     *
     * @param value 目标字串.
     * @return 结果.
     */
    public static String filterSymbols(String value) {
        value = StringUtils.filterCanSeeChar(value);
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
     *
     * @param value 目标字符.
     * @return 处理结果.
     */
    public static String encodeJsonCharset(String value) {
        value = StringUtils.filterCanSeeChar(value);
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder buff = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\'': {
                    buff.append('`');
                    break;
                }
                case '"': {
                    buff.append("``");
                    break;
                }
                default: {
                    buff.append(c);
                }
            }

        }

        return buff.toString();
    }

    /**
     * 构造 sphinxQL 全文索引中精确查询语句.
     *
     * @param value 目标字段.
     * @return 结果.
     */
    public static String buildPreciseQuery(StorageValue value, boolean useGroupName) {
        StringBuilder buff = new StringBuilder();
        ShortStorageName shortStorageName = value.shortStorageName();

        buff.append(shortStorageName.getPrefix())
            .append(filterSymbols(value.value().toString()));
        if (useGroupName) {
            buff.append(shortStorageName.getNoLocationSuffix()).append("*");
        } else {
            buff.append(shortStorageName.getSuffix());
        }

        return buff.toString();
    }

    /**
     * 构造 sphinxQL 全文索引中的分词模糊查询语法构造.
     * 只能处理StorageValue.STRING类型.
     *
     * @param tokenizer 分词器.
     * @return 查询语法.
     * @see StorageType
     */
    public static String buildSegmentationQuery(StorageValue value, Tokenizer tokenizer) {
        StringBuilder buff = new StringBuilder();
        ShortStorageName shortStorageName = value.shortStorageName();

        String strValue = filterSymbols(value.value().toString());
        Iterator<String> words = tokenizer.tokenize(strValue);
        buff.append('(');
        int emptyLen = buff.length();
        while (words.hasNext()) {
            if (buff.length() > emptyLen) {
                buff.append(" << ");
            }
            buff.append(shortStorageName.getPrefix())
                .append(words.next())
                .append(shortStorageName.getSuffix());
        }
        // 无法分词,使用原始字符.
        if (buff.length() == emptyLen) {
            buff.append(shortStorageName.getPrefix())
                .append(value.value().toString())
                .append(shortStorageName.getSuffix());
        }

        buff.append(')');

        return buff.toString();
    }

    /**
     * 通配符查询.
     *
     * @param value 查询目标值.
     * @return 查询语法.
     */
    public static String buildWirdcardQuery(StorageValue value) {
        return buildPreciseQuery(value, false);
    }
}
