package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.tokenizer.segmentation.JcsegTokenizer;
import com.xforceplus.ultraman.oqsengine.tokenizer.wildcard.WildcardTokenizer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 默认的分词器构造工厂.
 *
 * @author dongbin
 * @version 0.1 2021/3/16 11:07
 * @since 1.8
 */
public class DefaultTokenizerFactory implements TokenizerFactory {

    private ConcurrentMap<Integer, WildcardTokenizer> wildcardTokenizerCache = new ConcurrentHashMap<>();
    private Tokenizer segmentationTokenizer;


    /**
     * 指定通配符宽度和默认分词字典构造工厂.
     *
     * @throws IOException 构造失败.
     */
    public DefaultTokenizerFactory() throws IOException {
        segmentationTokenizer = new JcsegTokenizer();
    }

    /**
     * 指定通配符宽度和一个额外的词典地址.
     *
     * @param lexUrl 词典地址.
     * @throws IOException 构造失败.
     */
    public DefaultTokenizerFactory(URL lexUrl) throws IOException {
        segmentationTokenizer = new JcsegTokenizer(lexUrl);
    }

    /**
     * 读取本地文件加载分词字典.
     *
     * @param lexDir 字典目录.
     * @throws IOException 构造失败.
     */
    public DefaultTokenizerFactory(File lexDir) throws IOException {
        segmentationTokenizer = new JcsegTokenizer(lexDir);
    }

    @Override
    public Tokenizer getTokenizer(IEntityField field) {
        FieldConfig.FuzzyType type = field.config().getFuzzyType();
        switch (type) {
            case WILDCARD:
                return findWildcardTokenizer(field);
            case SEGMENTATION:
                return segmentationTokenizer;
            case NOT:
                return NothingTokenizer.getInstance();
            default:
                return UnsupportTokenizer.getInstance();
        }
    }

    public Map<Integer, WildcardTokenizer> getWildcardTokenizerCache() {
        return new HashMap<>(wildcardTokenizerCache);
    }

    public Tokenizer getSegmentationTokenizer() {
        return segmentationTokenizer;
    }

    /**
     * 构造字段要求的通配符分词器.
     */
    private Tokenizer findWildcardTokenizer(IEntityField field) {
        int min = field.config().getWildcardMinWidth();
        int max = field.config().getWildcardMaxWidth();
        List<Tokenizer> tokenizers = new ArrayList<>(max - min);
        for (int i = min; i <= max; i++) {
            tokenizers.add(wildcardTokenizerCache.computeIfAbsent(i, k -> new WildcardTokenizer(k)));
        }
        return new WrapperTokenizer(tokenizers, FieldConfig.FuzzyType.WILDCARD);
    }
}
