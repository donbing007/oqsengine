package com.xforceplus.ultraman.oqsengine.core.service.utils;

import java.util.Comparator;
import java.util.Iterator;

/**
 * merged iterator.
 *
 * @param <T> 元素.
 */
public class MergedIterator<T> implements Iterator<T> {

    private Iterator<T> s1;
    private Iterator<T> s2;
    private T next1;
    private T next2;
    private Comparator<T> comparator;
    private boolean isAsc;

    /**
     * 实例化.
     *
     * @param s1         迭代器1.
     * @param s2         迭代器2.
     * @param comparator 比较器.
     * @param isAsc      true降序,false升序.
     */
    public MergedIterator(Iterator<T> s1, Iterator<T> s2, Comparator<T> comparator, boolean isAsc) {
        if (s1 == null || s2 == null || comparator == null) {
            throw new IllegalArgumentException("arguments can't be null");
        }
        this.s1 = s1;
        this.s2 = s2;

        next1 = s1.hasNext() ? s1.next() : null;
        next2 = s2.hasNext() ? s2.next() : null;

        this.comparator = comparator;
        this.isAsc = isAsc;
    }

    @Override
    public boolean hasNext() {
        return next1 != null || next2 != null;
    }

    @Override
    public T next() {
        boolean useStream1 =
            next1 != null && next2 == null || next1 != null && (isAsc == (comparator.compare(next1, next2) <= 0));

        if (useStream1) {
            T returnObject = next1;
            next1 = s1.hasNext() ? s1.next() : null;
            return returnObject;
        } else {
            T returnObject = next2;
            next2 = s2.hasNext() ? s2.next() : null;
            return returnObject;
        }
    }
}
