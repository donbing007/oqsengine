package com.xforceplus.ultraman.oqsengine.tokenizer.segmentation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.tokenizer.EmptyWorkdsIterator;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
 * 储存模式将进行较细节的切分, 搜索模式将较为粗.
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

    private void init() throws IOException {
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
    public Iterator<String> tokenize(String value, TokenizerMode mode) {
        if (value == null || value.isEmpty()) {
            return EmptyWorkdsIterator.getInstance();
        }

        if (TokenizerMode.SEARCH == mode) {

            try {
                return new JcsegIterator(config, dic, ISegment.NLP, value);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

        } else if (TokenizerMode.STORAGE == mode) {
            JcsegIterator nlpIter;
            JcsegIterator mostIter;
            try {
                nlpIter = new JcsegIterator(config, dic, ISegment.NLP, value);
                mostIter = new JcsegIterator(config, dic, ISegment.MOST, value);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            Map<String, Object> nlpWords =
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(nlpIter, Spliterator.ORDERED), false)
                    .collect(Collectors.toMap(s -> s, s -> "", (s0, s1) -> s0, LinkedHashMap::new));
            // 需要关注的字符长度.
            final int watchLen = 1;
            Collection<String>
                mostWords =
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(mostIter, Spliterator.ORDERED), false)
                    .filter(word -> !nlpWords.containsKey(word))
                    .filter(word -> {

                        if (word.length() == watchLen || isEnOrNumber(word)) {
                            // 单字
                            return nlpWords.containsKey(word);

                        } else {
                            return true;
                        }
                    }).collect(Collectors.toList());

            List<String> results = new ArrayList<>(nlpWords.size() + mostWords.size());
            results.addAll(nlpWords.keySet());
            results.addAll(mostWords);
            return results.iterator();
        } else {
            return EmptyWorkdsIterator.getInstance();
        }
    }

    /**
     * 判定每一个字符.
     * 48-57 判定为数字.
     * 65-90 判定为大写字母.
     * 97-122 判定为小写字母.
     */
    private boolean isEnOrNumber(String word) {
        for (char c : word.toCharArray()) {
            if (isAlphabets(c)) {
                return true;
            }
            if (isNumber(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAlphabets(char c) {
        return (c >= 65 && c <= 90) || (c >= 97 && c <= 122);
    }

    private boolean isNumber(char c) {
        return c >= 48 && c <= 57;
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

        public JcsegIterator(SegmenterConfig config, ADictionary dic, ISegment.Type type, String value)
            throws IOException {
            this.config = config;
            this.dic = dic;
            this.value = value;
            seg = type.factory.create(config, dic);
            seg.reset(new StringReader(this.value));
            nextWord = seg.next();

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
