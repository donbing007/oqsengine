package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 表示一个 entity 的属性值.
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 *  @param <T> 实际类型.
 */
public interface IValue<T> {

    /**
     * 属性值相关的字段信息.
     * @return 字段信息.
     */
    public IEntityField getField();

    public void setField(IEntityField field);

    public void setValue(String value);

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


    IValue<T> shallowClone();

    default boolean compareByString(){
        return true;
    }

}
