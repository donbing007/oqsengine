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
     * 分词.
     * 如果当前分词器不支持当前值进行分词,那么将会空返回.
     *
     * @param value 需要分词的目标字串.
     * @return 分词结果.
     * @throws IOException 发生IO异常.
     */
    public Iterator<String> tokenize(String value);

    /**
     * 支持的模糊类型.
     *
     * @return 模糊类型.
     */
    public FieldConfig.FuzzyType support();
}
