package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Entity实体值对象.
 *
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public interface IEntityValue extends Cloneable {

    /**
     * 当前字段数量.
     *
     * @return 字段数量.
     */
    public int size();

    /**
     * 根据字段实例获取实段实例.
     *
     * @param field 字段.
     * @return 实例.
     */
    public Optional<IValue> getValue(IEntityField field);

    /**
     * 获得该对象指定属性的数据.
     * 无法获取特殊字段的值, 比如ID等特殊字段.请使用替代的<pre>getValue(IEntityField)}</pre>
     *
     * @param fieldName 字段名称.
     * @return 值.
     */
    public Optional<IValue> getValue(String fieldName);

    /**
     * 根据字段 id 查询当前值.
     * 无法获取特殊字段的值, 比如ID等特殊字段.请使用替代的<pre>getValue(IEntityField)}</pre>
     *
     * @param fieldId 字段 id.
     * @return 值.
     */
    public Optional<IValue> getValue(long fieldId);

    /**
     * 添加单个值对象.
     * 相同的字段将被替换.
     *
     * @param value 逻辑值.
     * @return 本身.
     */
    public IEntityValue addValue(IValue value);

    /**
     * 数据对象的数据信息.
     *
     * @return IValue对象
     */
    public Collection<IValue> values();

    /**
     * 添加多个值对象.
     *
     * @param values 多个逻辑值实例.
     * @return 本身.
     */
    public IEntityValue addValues(Collection<IValue> values);

    /**
     * 删除某个属性值.
     *
     * @param field 目标字段信息.
     * @return 被删除的值.
     */
    public Optional<IValue> remove(IEntityField field);

    /**
     * 根据条件过滤掉不需要的.
     *
     * @param predicate 条件.
     */
    public void filter(Predicate<? super IValue> predicate);

    /**
     * 清空当前的所有属性值.
     */
    public IEntityValue clear();

    /**
     * 扫描所有 IValue 实例,实执行指定逻辑.
     *
     * @param action 动作.
     */
    public default void scan(Consumer<? super IValue> action) {
        for (IValue v : values()) {
            action.accept(v);
        }
    }

    /**
     * 判断是否为脏.
     *
     * @return 是否为脏.
     */
    public boolean isDirty();

    /**
     * 设置状态为干净.
     */
    public default void neat() {
        for (IValue v : values()) {
            v.neat();
        }
    }

    /**
     * 将空值挤出,方法执行后不会再包含空值.
     */
    public void squeezeEmpty();

    /**
     * 克隆.
     */
    public Object clone() throws CloneNotSupportedException;
}
