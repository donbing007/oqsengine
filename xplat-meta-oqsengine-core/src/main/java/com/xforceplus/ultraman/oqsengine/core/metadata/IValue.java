package com.xforceplus.ultraman.oqsengine.core.metadata;

/**
 * 表示一个 entity 的属性值.
 * @param <T> 实际类型.
 */
public interface IValue<T> {

    /**
     * 获得属性名
     * @return 属性名称.
     */
    public String getName();

    /**
     * 根据属性名拿到值
     * @return 值.
     */
    public T getValue();

    /**
     * 将值转成String输出，所有类型都可以转成String
     * @return
     */
    public String valueToString();

    /**
     * 将值转成Long输出，String类型不允许转成Long，其他类型可以转成Long
     * @return
     */
    public long valueToLong();

}
