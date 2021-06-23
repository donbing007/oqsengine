package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.common.string.StringUtils;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.ShortStorageName;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * SphinxQL帮助类.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 18:41
 * @since 1.8
 */
public class SphinxQLHelper {

    /**
     * 标记这是一个支持模糊搜索的拆分词.
     */
    private static char FUZZY_WORD_FLAG = 'w';

    /**
     * 半角空格不可过滤,只有全角空格需要过滤.
     */
    protected static final int[] IGNORE_SYMBOLS = {
        '\'', '\\', '\"', '+', '#', '%', '~', '_', '±', '×', '÷', '=', '≠', '≡', '≌', '≈',
        '<', '>', '≮', '≯', '≤', '≥', '‰', '∞', '∝', '√', '∵', '∴', '∷', '∠', '⌒', '⊙', '○', 'π', '△', '⊥', '∪', '∩',
        '∫', '∑', '°', '′', '″', '℃', '{', '}', '(', ')', '[', ']', '|', '‖', '*', '/', ':', ';', '?', '!', '&', '～',
        '§', '→', '^', '$', '@', '`', '❤', '❥', '︼', '﹄', '﹂', 'ˉ', '︾', '︺', '﹀', '︸', '︶', '︻', '﹃', '﹁',
        // 全角
        '！', '＂', '＃', '＄', '％', '＆', '＇', '（', '）', '＊', '＋', '－', '．', '／', '：', '；', '＜', '＝', '＞', '？',
        '＠', '［', '＼', '］', '＾', '＿', '｀', '｛', '｜', '｝', '～', '　',
    };

    protected static final Map<Character, String> REPLACE_SYMBOLS;

    static {
        Arrays.sort(IGNORE_SYMBOLS);

        /*
         * 替换成英文表示.
         */
        REPLACE_SYMBOLS = new HashMap<>();
        REPLACE_SYMBOLS.put('-', "M");
        REPLACE_SYMBOLS.put('.', "D");
    }

    /**
     * 过滤所有不合式的符号.替换需要的字符为合式的字符.
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
        String replaceString;
        for (char c : value.toCharArray()) {
            if (Arrays.binarySearch(IGNORE_SYMBOLS, c) < 0) {
                replaceString = REPLACE_SYMBOLS.get(c);
                if (replaceString != null) {
                    buff.append(replaceString);
                } else {
                    buff.append(c);
                }
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
     * 编码分词结果的词.
     *
     * @param shortStorageName 字段短名称.
     * @param word             词.
     * @return 编码后的结果.
     */
    public static String encodeFuzzyWord(ShortStorageName shortStorageName, String word) {
        StringBuilder buff = new StringBuilder();

        buff.append(shortStorageName.getPrefix())
            .append(word)
            .append(FUZZY_WORD_FLAG)
            .append(shortStorageName.getSuffix());

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
        /*
         * 如果使用组名的话,忽略尾部定位序号.
         */
        if (useGroupName) {
            buff.append(shortStorageName.getNoLocationSuffix());
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
            buff.append(encodeFuzzyWord(shortStorageName, words.next()));
        }
        // 无法分词,使用原始字符.
        if (buff.length() == emptyLen) {
            buff.append(encodeFuzzyWord(shortStorageName, value.value().toString()));
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
        ShortStorageName shortStorageName = value.shortStorageName();

        return encodeFuzzyWord(shortStorageName, filterSymbols(value.value().toString()));
    }

    /**
     * 构造字段的搜索.
     *
     * @param value     目标字段值.
     * @param code      字段代码.
     * @param tokenizer 分词器.
     * @return java.lang.String 结果.
     * @author dongbin
     */
    public static String buildSearch(String value, String code, Tokenizer tokenizer) {

        String strValue = filterSymbols(value);
        Iterator<String> words = tokenizer.tokenize(strValue);
        StringBuilder buff = new StringBuilder();
        buff.append("@").append(FieldDefine.ATTRIBUTEF).append(' ');
        int emptyLen = buff.length();
        while (words.hasNext()) {
            if (buff.length() > emptyLen) {
                buff.append(" << ");
            }
            buff.append(code)
                .append(words.next())
                .append(code);
        }
        // 无法分词,使用原始字符.
        if (buff.length() == emptyLen) {
            buff.append(code)
                .append(value)
                .append(code);
        }

        return buff.toString();
    }

    /**
     * 计算sphinxql的查询数据总量.必须紧跟查询语句.
     *
     * @param resource 当前资源.
     * @return 数量.
     * @throws SQLException 发生异常.
     */
    public static long count(TransactionResource resource) throws SQLException {

        long count = 0;
        final String targetKey = "total_found";

        Connection conn = (Connection) resource.value();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery(SQLConstant.SELECT_COUNT_SQL)) {
                while (rs.next()) {
                    if (targetKey.equals(rs.getString("Variable_name"))) {
                        count = rs.getLong("Value");
                        break;
                    }
                }
            }
        }
        return count;
    }
}
