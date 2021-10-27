package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import java.util.Iterator;

/**
 * 实际进行任何分词的实现,只为了占位.
 *
 * @author dongbin
 * @version 0.1 2021/3/16 11:58
 * @since 1.8
 */
public class NothingTokenizer implements Tokenizer {

    private static final Tokenizer INSTANCE = new NothingTokenizer();
    private Iterator<String> emptyIterator = new EmptyWorkdsIterator();

    public static Tokenizer getInstance() {
        return INSTANCE;
    }

    @Override
    public Iterator<String> tokenize(String value, TokenizerMode mode) {
        return emptyIterator;
    }

    @Override
    public FieldConfig.FuzzyType support() {
        return FieldConfig.FuzzyType.NOT;
    }
}
