package com.xforceplus.ultraman.oqsengine.core.metadata;

import java.util.List;
import java.util.Map;

public interface IEntityValue<K,V> {
    /**
     * 获得数据对象的id
     * @return 数据对象的id
     */
    public Long id();

    /**
     * 获得数据对象下的子数据对象集合
     * @return 子数据对象数据集合 - 子数据对象目前不继续往下钻
     */
    public List<IEntityValue> entityValues();

    /**
     * 数据对象的数据信息
     * @return Map对象
     */
    public Map<K, V> values();
}
