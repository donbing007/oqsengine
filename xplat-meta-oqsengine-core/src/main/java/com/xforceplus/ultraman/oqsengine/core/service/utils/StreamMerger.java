package com.xforceplus.ultraman.oqsengine.core.service.utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * stream merger.
 *
 * @param <T> 元素.
 */
public class StreamMerger<T> {

    /**
     * 合并.
     *
     * @param stream1    流1.
     * @param stream2    流2.
     * @param comparator 比较器.
     * @param isAsc      true降序,false升序.
     * @return 新的流.
     */
    public Stream<T> merge(Stream<T> stream1, Stream<T> stream2, Comparator<T> comparator, boolean isAsc) {
        Iterator<T> iterator = new MergedIterator<>(stream1.iterator(), stream2.iterator(), comparator, isAsc);
        Spliterator<T> spliterator = new SpliteratorAdapter<>(iterator);
        return StreamSupport.stream(spliterator, false);
    }
}
