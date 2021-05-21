package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 表示一个 entity 的属性值.
 *
 * @param <T> 实际类型.
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public interface IValue<T> {

    /**
     * 属性值相关的字段信息.
     *
     * @return 字段信息.
     */
    public IEntityField getField();

    /**
     * 修改当前数据字段.
     *
     * @param field 新的字段.
     */
    public void setField(IEntityField field);

    /**
     * 设置字符串表示的值.
     *
     * @param value 新的字符串表示的值.
     */
    public void setStringValue(String value);

    /**
     * 根据属性名拿到值.
     *
     * @return 值.
     */
    public T getValue();

    /**
     * 将值转成String输出，所有类型都可以转成String.
     */
    public String valueToString();

    /**
     * 将值转成Long输出，String类型不允许转成Long，其他类型可以转成Long.
     */
    public long valueToLong();

    /**
     * 浅copy.
     *
     * @return 新的值实例.
     */
    public IValue<T> shallowClone();

    /**
     * 是否可能转型成字符串表示.
     *
     * @return true可以, false不可以..
     */
    public default boolean compareByString() {
        return true;
    }
}
