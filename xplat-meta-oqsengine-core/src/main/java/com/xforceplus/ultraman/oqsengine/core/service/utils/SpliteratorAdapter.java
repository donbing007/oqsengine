package com.xforceplus.ultraman.oqsengine.core.service.utils;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * spliter
 * @param <T>
 */
public class SpliteratorAdapter<T> extends Spliterators.AbstractSpliterator<T> {

    private final Iterator<T> iterator;

    protected SpliteratorAdapter(Iterator<T> iter) {
        super(Long.MAX_VALUE, 0);
        iterator = iter;
    }

    @Override
    public synchronized boolean tryAdvance(Consumer<? super T> action) {
        if (iterator.hasNext()) {
            action.accept(iterator.next());
            return true;
        }
        return false;
    }
}
