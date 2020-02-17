package com.xforceplus.ultraman.oqsengine.core.metadata;

import java.util.List;
import java.util.Map;

public interface IEntityValue {
    /**
     * 获得数据对象的id
     * @return 数据对象的id
     */
    public Long id();

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
    public IEntityValue setValue(IValue value,String fieldType);

    /**
     * 数据对象的数据信息
     * @return IValue对象
     */
    public List<IValue> values();

    /**
     * 添加多个值对象
     * @param values
     * @return
     */
    public IEntityValue setValues(List<IValue> values);
}
