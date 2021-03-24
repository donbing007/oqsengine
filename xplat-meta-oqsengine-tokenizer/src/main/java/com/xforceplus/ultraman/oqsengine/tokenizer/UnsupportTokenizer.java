package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;

import java.util.Iterator;

/**
 * 不支持的模糊类型分词器,此实现只为标示无法处理的模糊类型.
 *
 * @author dongbin
 * @version 0.1 2021/3/16 11:10
 * @since 1.8
 */
public class UnsupportTokenizer implements Tokenizer {

    private static final Tokenizer INSTANCE = new UnsupportTokenizer();

    /**
     * 获取实例.
     *
     * @return 分词器实例.
     */
    public static Tokenizer getInstance() {
        return INSTANCE;
    }

    @Override
    public Iterator<String> tokenize(String value) {
        throw new UnsupportedOperationException("Unknown fuzzy type.");
    }

    @Override
    public FieldConfig.FuzzyType support() {
        return FieldConfig.FuzzyType.UNKNOWN;
    }
}
