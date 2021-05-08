package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;

import java.util.Iterator;

/**
 * 什么都不做的分词.主要占位之用.
 *
 * @author dongbin
 * @version 0.1 2021/3/16 11:58
 * @since 1.8
 */
public class NothingTokenizer implements Tokenizer {

    private static final Tokenizer INSTANCE = new NothingTokenizer();
    private Iterator<String> emptyIterator = new EmptyWorkdsIterator();

    @Override
    public Iterator<String> tokenize(String value) {
        return emptyIterator;
    }

    @Override
    public FieldConfig.FuzzyType support() {
        return FieldConfig.FuzzyType.NOT;
    }

    static class EmptyIterator implements Iterator<String> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public String next() {
            return null;
        }
    }
}
