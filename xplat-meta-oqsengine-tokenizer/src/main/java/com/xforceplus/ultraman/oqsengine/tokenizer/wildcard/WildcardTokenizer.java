package com.xforceplus.ultraman.oqsengine.tokenizer.wildcard;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.tokenizer.EmptyWorkdsIterator;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;
import java.util.Iterator;
import java.util.Objects;

/**
 * 通配符的分词器,会将字串拆分成指定大小的子串.例如默认长度为3的情况下.
 * abcde -> abc bcd cde
 * 这是测试 -> 这是测 是测试
 * 即最终的关键字数量 (字符长度 - 子串长度) + 1
 * <p>
 * 储存模式和搜索模式没有区别.
 *
 * @author dongbin
 * @version 0.1 2021/3/15 13:55
 * @since 1.8
 */
public class WildcardTokenizer implements Tokenizer {

    private static final int DEFAULT_WIDTH = 3;
    private int width;

    public WildcardTokenizer() {
        this(DEFAULT_WIDTH);
    }

    /**
     * 实例化.
     *
     * @param width 字符宽度.
     */
    public WildcardTokenizer(int width) {
        this.width = width;

        if (this.width < DEFAULT_WIDTH) {
            throw new IllegalArgumentException(String.format("The maximum segment length is %d.", DEFAULT_WIDTH));
        }
    }

    @Override
    public Iterator<String> tokenize(String value, TokenizerMode mode) {
        if (value == null || value.length() < width) {

            return EmptyWorkdsIterator.getInstance();

        } else {

            return new WordsIterator(width, value);
        }
    }

    @Override
    public FieldConfig.FuzzyType support() {
        return FieldConfig.FuzzyType.WILDCARD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WildcardTokenizer)) {
            return false;
        }
        WildcardTokenizer that = (WildcardTokenizer) o;
        return width == that.width;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WildcardTokenizer{");
        sb.append("width=").append(width);
        sb.append('}');
        return sb.toString();
    }

    private static class WordsIterator implements Iterator<String> {

        /**
         * 目标字串.
         */
        private String value;
        /**
         * 当前处理的开始位置,从0开始.
         */
        private int currentPoint;
        /**
         * 已经被使用的子串数量.
         */
        private int useWordNumber;
        /**
         * 子串最小允许长度.
         */
        private int width;
        /**
         * 总共的子串数量.
         */
        private int totalWords;
        /**
         * 字符缓冲.
         */
        private char[] buff;

        public WordsIterator(int width, String value) {
            this.width = width;
            this.buff = new char[width];
            this.useWordNumber = 0;
            this.currentPoint = 0;
            this.totalWords = calculateTotalWords(value, width);

            this.value = value;
        }


        @Override
        public boolean hasNext() {
            return this.useWordNumber < this.totalWords;
        }

        @Override
        public String next() {
            for (int i = 0; i < width; i++) {
                buff[i] = value.charAt(currentPoint + i);
            }

            useWordNumber++;
            currentPoint++;
            return new String(buff);
        }
    }

    private static int calculateTotalWords(String value, int width) {
        return value.length() - (width - 1);
    }

}
