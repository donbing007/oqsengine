package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.common.StringUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLStringStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.ShortStorageName;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
     * select的单个关键字最大长度.
     */
    public static final int MAX_WORLD_SPLIT_LENGTH = 29;

    /**
     * 标记这是一个支持模糊搜索的拆分词.
     */
    private static char FUZZY_WORD_FLAG = 'w';

    /**
     * 标记这是一个附件.
     */
    private static char ATTACHMENT_WORD_FLAG = 'a';

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
        return encodeWord(shortStorageName, word, FUZZY_WORD_FLAG);
    }

    /**
     * 编码附件的词.
     *
     * @param shortStorageName 字段短名称.
     * @param word             词.
     * @return 编码后的结果.
     */
    public static String encodeAttachmentWord(ShortStorageName shortStorageName, String word) {
        return encodeWord(shortStorageName, word, ATTACHMENT_WORD_FLAG);
    }

    /**
     * 构造附件条件查询.
     *
     * @param value 目标物理值.
     * @return 结果.
     */
    public static String buildAttachemntQuery(StorageValue value) {
        return encodeAttachmentWord(value.shortStorageName(), (String) value.value());
    }

    /**
     * 构造 sphinxQL 全文索引中精确查询语句.
     *
     * @param value 目标字段.
     * @return 结果.
     */
    public static String buildPreciseQuery(StorageValue value, boolean useGroupName) {
//        StringBuilder buff = new StringBuilder();
//        ShortStorageName shortStorageName = value.shortStorageName();
//        buff.append(shortStorageName.getPrefix())
//            .append(filterSymbols(value.value().toString()));
//
//        /*
//         * 如果使用组名的话,忽略尾部定位序号.
//         */
//        if (useGroupName) {
//            buff.append(shortStorageName.getNoLocationSuffix());
//        } else {
//            buff.append(shortStorageName.getSuffix());
//        }
//
//        return buff.toString();

        return stringConditionFormat(value.value().toString(), value.shortStorageName(), useGroupName);
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

    private static String encodeWord(ShortStorageName shortStorageName, String word, char flag) {
        StringBuilder buff = new StringBuilder();

        buff.append(shortStorageName.getPrefix())
            .append(word)
            .append(flag)
            .append(shortStorageName.getSuffix());

        return buff.toString();
    }

    private static final char START = '[';
    private static final char END = ']';

    /**
     * strings value通用的转换(StorageValue)逻辑.
     * @param storageName 字段存储名称.
     * @param originValue 字段存储值（origin）.
     * @param attachment 是否附件.
     * @return 存储结构.
     */
    public static StorageValue stringsStorageConvert(String storageName, String originValue, boolean attachment) {

        String logicName = AnyStorageValue.getInstance(storageName).logicName();

        if (attachment) {
            return new StringStorageValue(logicName, originValue, true);
        }

        StringBuilder buff = new StringBuilder();
        StorageValue head = null;
        int location = 0;
        boolean watch = false;
        for (int i = 0; i < originValue.length(); i++) {
            char point = originValue.charAt(i);
            if (START == point) {
                watch = true;
                continue;
            }

            if (END == point) {
                watch = false;

                StorageValue newStorageValue = new StringStorageValue(logicName, buff.toString(), true);
                newStorageValue.locate(location++);

                if (head == null) {
                    head = newStorageValue;
                } else {
                    head.stick(newStorageValue);
                }

                buff.delete(0, buff.length());
                continue;
            }

            if (watch) {
                buff.append(point);
            }
        }

        return head;
    }

    /**
     *
     * @param word
     * @param shortStorageName
     * @param useGroupName
     * @return
     */
    public static String stringConditionFormat(String word, ShortStorageName shortStorageName, boolean useGroupName) {
        String[] values = longStringWrap(word, MAX_WORLD_SPLIT_LENGTH);

        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirst = true;
        for (String v : values) {
            if (!isFirst) {
                stringBuilder.append(" >> ");
            }
            stringBuilder.append(shortStorageName.getPrefix())
                .append(filterSymbols(v));

            /*
             * 如果使用组名的话,忽略尾部定位序号.
             */
            if (useGroupName) {
                stringBuilder.append(shortStorageName.getNoLocationSuffix());
            } else {
                stringBuilder.append(shortStorageName.getSuffix());
            }

            isFirst = false;
        }
        return stringBuilder.toString();
    }

    /**
     * 将超长字段拆分为固定格式, 比如ABC -> [A][B][C].
     *
     * @param word
     * @return
     */
    public static String stringValueFormat(String word) {
        String[] values = longStringWrap(word, MAX_WORLD_SPLIT_LENGTH);

        StringBuilder stringBuilder = new StringBuilder();
        for (String v : values) {
            stringBuilder.append(START)
                .append(v)
                .append(END);
        }
        return stringBuilder.toString();
    }

    /**
     * 切割一个长字符串, 按照传入的长度.
     *
     * @param word 字符串.
     * @param length 字符串最大允许长度.
     * @return 切割后的字符串数组.
     */
    private static String[] longStringWrap(String word, int length) {
        int fullSize = word.length() / length;
        if (0 != word.length() % length) {
            fullSize += 1;
        }

        String[] res = new String[fullSize];
        for (int index = 0; index < fullSize; index++) {
            res[index] = substring(word, index * length, (index + 1) * length);
        }

        return res;
    }

    /**
     * 分割字符串
     * @param str 字符串.
     * @param f 起始位置.
     * @param t 长度.
     * @return 切割后的字符串.
     */
    private static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }

}
