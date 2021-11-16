package com.xforceplus.ultraman.oqsengine.common.iterator;

import java.util.Iterator;

/**
 * 数据迭代器.
 *
 * @param <E> 元素.
 * @author dongbin
 * @version 0.1 2021/2/23 10:59
 * @since 1.8
 */
public interface DataIterator<E> extends Iterator<E> {

    /**
     * 数据总量.
     *
     * @return 数据量.
     */
    default long size() {
        return 0;
    }

    /**
     * 表示是否提供数据总量.
     *
     * @return true 提供, false size()方法返回恒为0.
     */
    default boolean provideSize() {
        return false;
    }

}
