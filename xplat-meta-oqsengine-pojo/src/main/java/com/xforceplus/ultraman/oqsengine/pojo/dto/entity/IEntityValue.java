package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

import java.util.Collection;
import java.util.List;

public interface IEntityValue {
    /**
     * 获得数据对象的id
     * @return 数据对象的id
     */
    public long id();

    /**
     * 获得该对象指定属性的数据
     * @param fieldName
     * @return
     */
    public IValue getValue(String fieldName);

    /**
     * 添加单个值对象
     * @param value
     * @return
     */
    public IEntityValue addValue(IValue value);

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
    public IEntityValue setValues(List<IValue> values);
}
