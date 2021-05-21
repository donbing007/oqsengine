package com.xforceplus.ultraman.oqsengine.tokenizer;

import java.util.Iterator;

/**
 * 没有任何分词结果迭代器.
 *
 * @author dongbin
 * @version 0.1 2021/3/15 14:44
 * @since 1.8
 */
public class EmptyWorkdsIterator implements Iterator<String> {

    private static final EmptyWorkdsIterator instance = new EmptyWorkdsIterator();

    public static EmptyWorkdsIterator getInstance() {
        return instance;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public String next() {
        return null;
    }
}
