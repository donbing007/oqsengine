package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

/**
 * Entity实体定义.
 *
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public interface IEntity extends Cloneable {

    /**
     * 获得本对象的id - 数据id.
     *
     * @return 数据对象的id.
     */
    public long id();

    /**
     * 获取实例元信息指示器.
     *
     * @return 本对象的元信息指示器.
     */
    public EntityClassRef entityClassRef();

    /**
     * 获得该对象的数据对象集合.
     *
     * @return 本对象的数据对象.
     */
    public IEntityValue entityValue();

    /**
     * 重置字段信息.
     *
     * @param enetityValue 新的字段信息.
     */
    public void resetEntityValue(IEntityValue enetityValue);

    /**
     * 重置 id.
     *
     * @param id 新的 id.
     */
    public void resetId(long id);

    /**
     * 当前数据版本号.
     *
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
     * 数据维护标识.
     *
     * @return 维护标识.
     */
    public long maintainId();

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
     * 记录当前时间.
     */
    public void markTime();

    /**
     * 产生的oqs大版本号.
     *
     * @return 大版本号.
     */
    public int major();

    /**
     * 克隆.
     */
    public Object clone() throws CloneNotSupportedException;

    /**
     * 维护ID.
     *
     * @param maintainId 维护ID
     */
    public void restMaintainId(long maintainId);

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
}
