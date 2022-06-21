package com.xforceplus.ultraman.oqsengine.pojo.dto.values.able;

import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

/**
 * 可以进行数学运算.
 *
 * @author dongbin
 * @version 0.1 2022/6/21 14:03
 * @since 1.8
 */
public interface CalculationsAble<V> {

    /**
     * 进行加法运算.
     * 最终返回一个新的IValue实例,其以当前被加数位置的IValue为蓝本.
     *
     * @param other 加数.
     * @return 新值.
     */
    public CalculationsAble<V> plus(IValue<V> other);

    /**
     * 进行减法运算.
     * 最终返回一个新的IValue实例,其以当前被加数位置的IValue为蓝本.
     *
     * @param other 减数.
     * @return 新值.
     */
    public CalculationsAble<V> subtract(IValue<V> other);

    /**
     * 当前值减1.
     *
     * @return 计算后的新值.
     */
    public CalculationsAble<V> decrement();

    /**
     * 当前值加1.
     *
     * @return 计算后的新值.
     */
    public CalculationsAble<V> increment();
}
