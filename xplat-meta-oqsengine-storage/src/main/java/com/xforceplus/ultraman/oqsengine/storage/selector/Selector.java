package com.xforceplus.ultraman.oqsengine.storage.selector;

/**
 * 选择器.
 *
 * @param <V> 选择目标元素类型.
 * @author dongbin
 * @version 0.1 2020/2/16 19:10
 * @since 1.8
 */
public interface Selector<V> {

    public V select(String key);
}
