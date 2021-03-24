package com.xforceplus.ultraman.oqsengine.common.iterator;

import java.util.Iterator;

/**
 * 数据迭代器.
 *
 * @param <E>
 * @author dongbin
 * @version 0.1 2021/2/23 10:59
 * @since 1.8
 */
public interface DataIterator<E> extends Iterator<E> {

    /**
     * 数据总量.
     *
     * @return
     */
    int size();

}
