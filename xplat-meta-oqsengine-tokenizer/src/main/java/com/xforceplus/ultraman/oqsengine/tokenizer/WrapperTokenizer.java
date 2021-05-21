package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 包装分词器,主要用以将多个分词器合并为同一个.
 *
 * @author dongbin
 * @version 0.1 2021/3/17 12:05
 * @since 1.8
 */
public class WrapperTokenizer implements Tokenizer {

    private Collection<Tokenizer> tokenizers;
    private FieldConfig.FuzzyType type;

    public WrapperTokenizer(Collection<Tokenizer> tokenizers, FieldConfig.FuzzyType type) {
        this.tokenizers = tokenizers;
        this.type = type;
    }

    @Override
    public Iterator<String> tokenize(String value) {
        return new WrapperIterator(tokenizers, value);
    }

    @Override
    public FieldConfig.FuzzyType support() {
        return type;
    }

    static class WrapperIterator implements Iterator<String> {

        private List<Tokenizer> tokenizers;
        private Iterator<String> currentIter;
        private String value;

        public WrapperIterator(Collection<Tokenizer> tokenizers, String value) {
            this.tokenizers = new ArrayList<>(tokenizers);
            this.value = value;

            if (!tokenizers.isEmpty()) {
                currentIter = this.tokenizers.remove(0).tokenize(this.value);
            }
        }

        @Override
        public boolean hasNext() {
            if (currentIter.hasNext()) {
                return true;
            } else {
                while (true) {
                    if (tokenizers.isEmpty()) {
                        return false;
                    }

                    Tokenizer tokenizer = tokenizers.remove(0);
                    currentIter = tokenizer.tokenize(value);
                    if (currentIter.hasNext()) {
                        return true;
                    }
                }
            }
        }

        @Override
        public String next() {
            return currentIter.next();
        }
    }
}
