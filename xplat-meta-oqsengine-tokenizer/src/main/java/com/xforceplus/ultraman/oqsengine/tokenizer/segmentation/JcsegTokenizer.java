package com.xforceplus.ultraman.oqsengine.tokenizer.segmentation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.tokenizer.EmptyWorkdsIterator;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.IWord;
import org.lionsoul.jcseg.dic.ADictionary;
import org.lionsoul.jcseg.dic.DictionaryFactory;
import org.lionsoul.jcseg.segmenter.SegmenterConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * 基于jcseg的分词器实现.
 *
 * @author dongbin
 * @version 0.1 2021/3/15 14:48
 * @since 1.8
 */
public class JcsegTokenizer implements Tokenizer {

    private SegmenterConfig config;
    private ADictionary dic;

    public JcsegTokenizer() throws IOException {
        init();
    }

    public JcsegTokenizer(File lexDir) throws IOException {
        init();
        dic.loadDirectory(lexDir.getAbsolutePath());
    }

    /**
     * 实例化.
     *
     * @param url 外部字典地址.
     * @throws IOException 加载异常.
     */
    public JcsegTokenizer(URL url) throws IOException {
        init();
        try (InputStream in = url.openStream()) {
            dic.load(in);
        }
    }

    private final void init() throws IOException {
        config = new SegmenterConfig(true);
        dic = DictionaryFactory.createDefaultDictionary(config, false);

        /*
         * 没有使用自带的
         * dic.loadClassPath()
         * 原因是其没法正确处理jar路径中的字典文件.
         */
        initFromDict();
    }

    private void initFromDict() throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource[] resources;
        resources = resolver.getResources("classpath*:/lexicon/*.lex");
        for (Resource resource : resources) {
            dic.load(resource.getInputStream());
        }
    }

    @Override
    public Iterator<String> tokenize(String value) {
        if (value == null || value.isEmpty()) {
            return EmptyWorkdsIterator.getInstance();
        } else {
            try {
                return new JcsegIterator(config, dic, value);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    public FieldConfig.FuzzyType support() {
        return FieldConfig.FuzzyType.SEGMENTATION;
    }

    /**
     * jcseg 分词迭代器.
     */
    private static class JcsegIterator implements Iterator<String> {
        private SegmenterConfig config;
        private ADictionary dic;
        private ISegment seg;

        private String value;
        private IWord nextWord;

        public JcsegIterator(SegmenterConfig config, ADictionary dic, String value) throws IOException {
            this.config = config;
            this.dic = dic;
            this.value = value;
            seg = ISegment.NLP.factory.create(config, dic);
            seg.reset(new StringReader(this.value));
            nextWord = seg.next();

            if (nextWord != null && nextWord.getValue().equals(value)) {
                nextWord = null;
            }
        }

        @Override
        public boolean hasNext() {
            return nextWord != null;
        }

        @Override
        public String next() {
            IWord current = nextWord;

            try {
                nextWord = seg.next();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            return current.getValue();
        }
    }
}
