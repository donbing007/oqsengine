package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 分词工厂.
 *
 * @author dongbin
 * @version 0.1 2021/3/16 11:05
 * @since 1.8
 */
public interface TokenizerFactory {

    /**
     * 获得支持指定类型的分词器.
     *
     * @param field 字段定义.
     * @return 分词器.
     */
    public Tokenizer getTokenizer(IEntityField field);

    /**
     * 获得基于语义分词的分词器.
     *
     * @return com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer
     */
    public Tokenizer getSegmentationTokenizer();

    /**
     * 获得通配符分词器.
     *
     * @param min 通配符的最小分配字符数量.
     * @param max 通配符的最大分配字符数量.
     * @return com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer
     */
    public Tokenizer getWildcardTokenizer(int min, int max);
}
