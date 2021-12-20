package com.xforceplus.ultraman.oqsengine.common.iterator;

import java.util.ArrayList;
import java.util.Collections;
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
    private long maxSize;
    private long cursor;
    private boolean more;

    /**
     * 初始化.
     *
     * @param buffSize 缓存大小.
     */
    public AbstractDataIterator(int buffSize) {
        this(buffSize, Long.MAX_VALUE);
    }

    /**
     * 构造一个新的迭代器实例.
     *
     * @param buffSize 缓存大小.
     * @param maxSize 最大上限.
     */
    public AbstractDataIterator(int buffSize, long maxSize) {
        if (buffSize > 0) {
            buff = new ArrayList<>(buffSize);
        } else {
            buff = Collections.emptyList();
        }
        this.buffSize = buffSize;
        this.maxSize = maxSize;
        this.cursor = 0;
        this.more = true;
    }

    @Override
    public long size() {
        return 0;
    }

    /**
     * 当前处理的从0开始的偏移量.
     *
     * @return 偏移量.
     */
    public long getCursor() {
        return cursor;
    }

    /**
     * 迭代器最大允许处理的数量.
     *
     * @return 最大数量.
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * 获得BUff大小.
     *
     * @return buff大小.
     */
    public int getBuffSize() {
        return buffSize;
    }

    @Override
    public boolean hasNext() {
        if (haveOutOfLimit()) {
            return false;
        }

        if (buff.isEmpty()) {
            if (!more) {
                return false;
            }

            try {
                load(buff, getBuffSize());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            if (buff.size() < getBuffSize()) {
                more = false;
            } else {
                more = true;
            }
        }

        return !buff.isEmpty();
    }

    @Override
    public E next() {
        if (haveOutOfLimit()) {
            return null;
        }

        E e = buff.remove(0);
        cursor++;
        return e;
    }

    /**
     * 当 hasNext() 为false时用以标记是否还有更多数据.
     * 当前的中断只是因为超出规定上限.
     *
     * @return true 还有更多, false没有更多了.
     */
    public boolean more() {
        return this.more;
    }

    /**
     * 加载数据.
     *
     * @param buff 目标buff.
     * @param limit 加载数据量上限.
     */
    protected abstract void load(List<E> buff, int limit) throws Exception;

    // 是否超出允许上限了.
    private boolean haveOutOfLimit() {
        return !(cursor < maxSize);
    }

}
