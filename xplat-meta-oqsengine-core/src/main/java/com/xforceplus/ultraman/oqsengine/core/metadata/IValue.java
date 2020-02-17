package com.xforceplus.ultraman.oqsengine.core.metadata;

public interface IValue<T> {

    /**
     * 获得属性名
     * @return
     */
    public String getName();

    /**
     * 根据属性名拿到值
     * @param name
     * @return
     */
    public T getValue(String name);

    /**
     * 设置属性名和值
     * @param name
     * @param value
     * @return
     */
    public T setValue(String name ,T value);

    /**
     * 将值转成String输出，所有类型都可以转成String
     * @return
     */
    public String valueToString();

    /**
     * 将值转成Long输出，String类型不允许转成Long，其他类型可以转成Long
     * @return
     */
    public Long valueToLong();

}
