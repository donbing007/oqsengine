package com.xforceplus.ultraman.oqsengine.storage.selector;

/**
 * 实际不进行选择.
 * 此实现的主要目的在于提供一个默认的 Selector 实现.
 *
 * @param <V> 选择元素类型.
 * @author dongbin
 * @version 0.1 2020/3/20 18:29
 * @since 1.8
 */
public class NoSelector<V> implements Selector<V> {

    @Override
    public V select(String key) {
        return (V) key;
    }
}
