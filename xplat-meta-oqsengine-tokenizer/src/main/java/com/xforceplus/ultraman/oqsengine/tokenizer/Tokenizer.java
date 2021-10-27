package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import java.io.IOException;
import java.util.Iterator;

/**
 * 分词器.
 *
 * @author dongbin
 * @version 0.1 2021/3/15 13:45
 * @since 1.8
 */
public interface Tokenizer {

    /**
     * 使用搜索模式进行分词.
     *
     * @param value 需要分词的目标字串.
     * @return 分词结果.
     * @throws IOException 发生IO异常.
     */
    public default Iterator<String> tokenize(String value) {
        return tokenize(value, TokenizerMode.SEARCH);
    }

    /**
     * 分词.
     *
     * @param value 当前需要分词的目标字符串.
     * @param mode  分词模式.
     * @return 分词结果.
     */
    public Iterator<String> tokenize(String value, TokenizerMode mode);

    /**
     * 支持的模糊类型.
     *
     * @return 模糊类型.
     */
    public FieldConfig.FuzzyType support();

    /**
     * 分词模式.
     */
    static enum TokenizerMode {
        /**
         * 未知,一般表示错误.
         */
        UNKNOWN(0),
        /**
         * 搜索模式,处理用户输入一般较分粗.
         */
        SEARCH(1),
        /**
         * 储存模式,处理原始字符,一般会进行较精细的切分.
         */
        STORAGE(2);

        private int symbol;

        private TokenizerMode(int symbol) {
            this.symbol = symbol;
        }

        public int getSymbol() {
            return symbol;
        }

        /**
         * 根据字面量获得实例.
         *
         * @param symbol 字面量.
         * @return 实例.
         */
        public static TokenizerMode getInstance(int symbol) {
            for (TokenizerMode mode : TokenizerMode.values()) {
                if (mode.getSymbol() == symbol) {
                    return mode;
                }
            }

            return TokenizerMode.UNKNOWN;
        }
    }
}
