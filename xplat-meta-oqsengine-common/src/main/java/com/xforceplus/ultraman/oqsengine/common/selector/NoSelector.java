package com.xforceplus.ultraman.oqsengine.common.selector;

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

    private V fixed;

    public NoSelector(V fixed) {
        this.fixed = fixed;
    }

    @Override
    public V select(String key) {
        return fixed;
    }

}
