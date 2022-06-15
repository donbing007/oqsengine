package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Optional;

/**
 * 表示一个 entity 的属性值.
 *
 * @param <T> 实际类型.
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public interface IValue<T> extends Comparable<IValue> {

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
     * 复制一个新的实例,其带有和当前实例相同的字段信息和值信息.
     *
     * @return 新的值实例.
     */
    public default IValue<T> copy() {
        return copy(getField());
    }

    /**
     * 构造一个新的实例, 使用新的附件.
     *
     * @param attachment 新的附件.
     * @return 新的实例.
     */
    public default IValue<T> copy(String attachment) {
        return copy(getField(), attachment);
    }

    /**
     * 构造一个新的实例,使用新的字段和当前实例的值和其附件.
     *
     * @param newField 新的目标字段.
     * @return 新实例.
     */
    public default IValue<T> copy(IEntityField newField) {
        return copy(newField, getAttachment().orElse(null));
    }

    /**
     * 复制一个新的实体.使用新的字段和新的附件,但是值为当前实例的.
     *
     * @param newField   新的字段.
     * @param attachment 新的附件.
     * @return 新实例.
     */
    public IValue<T> copy(IEntityField newField, String attachment);

    /**
     * 获取附件.
     *
     * @return 附件.
     */
    public Optional<String> getAttachment();

    /**
     * 判断是否"脏",表示修改但未持久.
     *
     * @return true 脏, false 干净.
     */
    public boolean isDirty();

    /**
     * 设置为脏对象.表示修改没有持久化.
     */
    public void dirty();

    /**
     * 设置为非脏对象.表示修改已经持久化了.
     */
    public void neat();

    /**
     * 是否可能转型成字符串表示.
     *
     * @return true可以, false不可以..
     */
    public default boolean compareByString() {
        return true;
    }

    @Override
    int compareTo(IValue o);
}
