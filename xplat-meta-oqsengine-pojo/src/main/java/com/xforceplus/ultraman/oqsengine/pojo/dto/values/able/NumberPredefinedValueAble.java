package com.xforceplus.ultraman.oqsengine.pojo.dto.values.able;

import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

/**
 * 表示可以获取预定义的值.
 *
 * @author dongbin
 * @version 0.1 2022/6/21 11:25
 * @since 1.8
 */
public interface NumberPredefinedValueAble<V> {

    /**
     * 返回一个表示当前字段的最大值实例.
     *
     * @return 最大值实例.
     */
    public IValue<V> max();

    /**
     * 返回一个表示当前字段最小值实例.
     *
     * @return 最小值实例.
     */
    public IValue<V> min();

}
