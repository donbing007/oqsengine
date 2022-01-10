package com.xforceplus.ultraman.oqsengine.tokenizer;

/**
 * 表示可以设置分词器工厂.
 *
 * @author dongbin
 * @version 0.1 2021/3/16 19:28
 * @since 1.8
 */
public interface TokenizerFactoryAble {

    /**
     * 设置分词器工厂.
     *
     * @param tokenizerFactory 分词器工厂.
     */
    public void setTokenizerFacotry(TokenizerFactory tokenizerFactory);
}
