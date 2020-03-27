package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Entity实体值对象.
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public interface IEntityValue extends Cloneable{
    /**
     * 获得数据对象的id
     * @return 数据对象的id
     */
    public long id();

    /**
     * 重置 id.
     */
    public void restId(long id);

    /**
     * 获得该对象指定属性的数据
     * @param fieldName
     * @return 值.
     */
    public Optional<IValue> getValue(String fieldName);

    /**
     * 根据字段 id 查询当前值.
     * @param fieldId 字段 id.
     * @return 值.
     */
    public Optional<IValue> getValue(long fieldId);

    /**
     * 添加单个值对象
     * @param value
     * @return
     */
    public IEntityValue addValue(IValue value);

    /**
     * 替换某个field值为
     */

    /**
     * 数据对象的数据信息
     * @return IValue对象
     */
    public Collection<IValue> values();

    /**
     * 添加多个值对象
     * @param values
     * @return
     */
    public IEntityValue addValues(Collection<IValue> values);

    /**
     * 删除某个属性值.
     * @param field 目标字段信息.
     * @return 被删除的值.
     */
    public IValue remove(IEntityField field);

    /**
     * 根据条件过滤掉不需要的.
     * @param predicate 条件.
     */
    public void filter(Predicate<? super IValue> predicate);

    /**
     * 清空当前的所有属性值.
     */
    public IEntityValue clear();

    /**
     * 克隆.
     * @return
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException;
}
