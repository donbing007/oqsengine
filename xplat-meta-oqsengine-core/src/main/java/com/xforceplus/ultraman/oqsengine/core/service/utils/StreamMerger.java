package com.xforceplus.ultraman.oqsengine.core.service.utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamMerger<T> {

    public Stream<T> merge(Stream<T> stream1, Stream<T> stream2, Comparator<T> comparator, boolean isAsc) {
        Iterator<T> iterator = new MergedIterator<>(stream1.iterator(), stream2.iterator(), comparator, isAsc);
        Spliterator<T> spliterator = new SpliteratorAdapter<>(iterator);
        return StreamSupport.stream(spliterator, false);
    }
}
