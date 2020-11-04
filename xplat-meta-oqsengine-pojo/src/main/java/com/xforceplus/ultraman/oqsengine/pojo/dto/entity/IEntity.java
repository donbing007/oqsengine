package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

/**
 * Entity实体定义.
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public interface IEntity extends Cloneable {

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
     * 重置字段信息.
     *
     * @param iEntityValue 新的字段信息.
     */
    public void resetEntityValue(IEntityValue iEntityValue);

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
     * 重置继承家族信息.
     * @param family 新家族.
     */
    public void resetFamily(IEntityFamily family);

    /**
     * 当前数据版本号.
     * @return 版本号.
     */
    public int version();

    /**
     * 重置版本号为指定值.
     *
     * @param version 指定版本号.
     */
    public void resetVersion(int version);

    /**
     * 最后处理时间戳.
     *
     * @return 时间戳.
     */
    public long time();

    /**
     * 标记时间戳.
     *
     * @param time 新的时间戮.
     */
    public void markTime(long time);

    /**
     * 克隆.
     * @return
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException;


}
