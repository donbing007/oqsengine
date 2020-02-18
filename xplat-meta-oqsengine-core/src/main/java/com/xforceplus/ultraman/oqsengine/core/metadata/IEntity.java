package com.xforceplus.ultraman.oqsengine.core.metadata;

import java.io.Serializable;
import java.util.List;

public interface IEntity extends Serializable {
    /**
     * 获得本对象的id - 数据id
     * @return 数据对象的id
     */
    public Long id();

    /**
     * 获得该对象结构对象
     * @return 本对象的结构对象
     */
    public IEntityClass entityClass();

    /**
     * 获得该对象的数据对象集合
     * @return 本对象的数据对象
     */
    public IEntityValue entityValue();

    /**
     * 值关联信息
     * @return 关联的对象
     */
    public ILink valueLink();

    /**
     * 继承关联-服务端
     * @return 关联的对象
     */
    public ILink refLink();
}
