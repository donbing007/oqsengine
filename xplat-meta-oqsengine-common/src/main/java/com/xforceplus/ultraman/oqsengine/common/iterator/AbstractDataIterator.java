package com.xforceplus.ultraman.oqsengine.common.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 数据迭代器的抽像实现,默认数据数量为0,即不提供数据数量的查询.
 * 子类需要实现 load 方法用以加载数据.
 *
 * @author dongbin
 * @version 0.1 2021/07/26 10:54
 * @since 1.8
 */
public abstract class AbstractDataIterator<E> implements DataIterator<E> {

    private int buffSize;
    private List<E> buff;

    /**
     * 初始化.
     *
     * @param buffSize 缓存大小.
     */
    public AbstractDataIterator(int buffSize) {
        if (buffSize > 0) {
            buff = new ArrayList<>(buffSize);
        } else {
            buff = Collections.emptyList();
        }
        this.buffSize = buffSize;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public boolean hasNext() {
        if (buff.isEmpty()) {
            load(buff, getBuffSize());
        }

        return !buff.isEmpty();
    }

    @Override
    public E next() {
        return buff.remove(0);
    }

    public int getBuffSize() {
        return buffSize;
    }

    /**
     * 加载数据.
     *
     * @param buff 目标buff.
     * @param limit 加载数据量上限.
     */
    protected abstract void load(List<E> buff, int limit);

}
