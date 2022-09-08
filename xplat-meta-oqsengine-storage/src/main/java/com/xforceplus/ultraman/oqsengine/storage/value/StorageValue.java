package com.xforceplus.ultraman.oqsengine.storage.value;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;

/**
 * 储存值类型.支持多值类型.
 *
 * <p>value0 --> value1.
 *
 * <p>StorageValue v1 = ...
 * StorageValue v2 = ...
 *
 * <p>v2 = v1.stick(v2); //返回v2的实例.
 *
 * @param <V> 实际值类型.
 * @author dongbin
 * @version 0.1 2020/3/4 12:21
 * @since 1.8
 */
public interface StorageValue<V> {

    /**
     * 分割标志.
     */
    public static final String PARTITION_FLAG = "P";

    /**
     * 分割点起始.
     */
    public static final int FIRST_PARTITION = 0;

    /**
     * 标识不是多值.
     */
    public static final int NOT_LOCATION = -1;

    /**
     * 标识不是长字符串分割.
     */
    public static final int NOT_PARTITION = -1;

    /**
     * 判断是否还有下个值.
     *
     * @return 下个值.
     */
    boolean haveNext();

    /**
     * 相关的下一个StorageValue.在逻辑上在上层表现为同一个值.
     *
     * @return 按顺序的下一个 StorageValue.
     */
    StorageValue<V> next();

    /**
     * 设定当前的下一个值.
     *
     * @param value 下一个值.
     */
    void next(StorageValue<V> value);

    /**
     * 将一个物理储存值实例合并到当前的值中.
     *
     * @param value 目标值.
     * @return 当前值的首个值实例.
     */
    StorageValue<V> stick(StorageValue<V> value);

    /**
     * 当前物理储存值的一个附件物理值表示.
     *
     * @return 附件物理值.
     */
    StorageValue<String> getAttachment();

    /**
     * 判断是否含有附件.
     *
     * @return true有附件, false没有附件.
     */
    boolean haveAttachment();

    /**
     * 修改附件.
     *
     * @param attachment 新的附件.
     */
    void setAttachment(StorageValue<String> attachment);

    /**
     * 字段逻辑名称.
     *
     * @return 名称.
     */
    String logicName();

    /**
     * 物理储存字段名称.
     *
     * @return 名称.
     */
    String storageName();

    /**
     * 物理储存字段短名称.
     *
     * @return 短名称.
     */
    ShortStorageName shortStorageName();

    /**
     * 组名称,此名称表示单个逻辑名称对应的所有物理名称的前辍.
     * 比如逻辑名称为 c1, 物理为 c1L0 c1L1 那么组名称即是 c1L
     *
     * @return 组名称.
     */
    String groupStorageName();

    /**
     * 值所处于的位置.这个位置意为多个 StorageValue 才能组成一个 IValue 时的顺序定位.
     *
     * @return 从0开始的位置.
     */
    int location();

    /**
     * 指定当前 value 所处于的位置序号.
     *
     * @param location 目标序号,从0开始.
     * @return 定位后的位置.从0开始.
     */
    int locate(int location);

    /**
     * 储存的字段值.
     *
     * @return 实际值.
     */
    V value();

    /**
     * 当前的储存类型.
     *
     * @return 类型.
     */
    StorageType type();

    /**
     * 设置不需要拼接location.
     */
    void notLocationAppend();

    /**
     * 设置当前部分为整个长字段切分中的第几部分.
     */
    void partition(int partition);

    /**
     * 判断是否空值.
     *
     * @return true 空值, false 非空值.
     */
    boolean isEmpty();
}
