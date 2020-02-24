package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import java.io.Serializable;

/**
 * entity 实体定义.
 */
public interface IEntity extends Serializable {

    /**
     * 获得本对象的id - 数据id
     * @return 数据对象的id
     */
    public long id();

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
     * 继承家族关系.
     * @return 家族信息.
     */
    public IEntityFamily family();

    /**
     * 重置 id.
     * @param id 新的 id.
     */
    public void resetId(long id);

    /**
     * 当前数据版本号.
     * @return 版本号.
     */
    public int version();
}
