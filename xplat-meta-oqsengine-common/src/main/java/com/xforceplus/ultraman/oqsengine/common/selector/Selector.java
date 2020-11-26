package com.xforceplus.ultraman.oqsengine.common.selector;

import java.util.Collections;
import java.util.List;

/**
 * 选择器.
 *
 * @param <V> 选择目标元素类型.
 * @author dongbin
 * @version 0.1 2020/2/16 19:10
 * @since 1.8
 */
public interface Selector<V> {

    /**
     * 选择预定的某个值.
     *
     * @param key 目标key.
     * @return 选择的值.
     */
    public V select(String key);

    default List<V> selects() {
        return Collections.emptyList();
    }

}
