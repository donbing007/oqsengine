package com.xforceplus.ultraman.oqsengine.storage.value;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;

/**
 * 储存值类型.支持多值类型.
 *
 * value0 --> value1.
 *
 * StorageValue v1 = ...
 * StorageValue v2 = ...
 *
 * v2 = v1.stick(v2); //返回v2的实例.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 12:21
 * @since 1.8
 */
public interface StorageValue<V> {

    /**
     * 标识不是多值.
     */
    public static final int NOT_LOCATION = -1;

    /**
     * 判断是否还有下个值.
     * @return 下个值.
     */
    boolean haveNext();

    /**
     * 相关的下一个StorageValue.在逻辑上在上层表现为同一个值.
     * @return 按顺序的下一个 StorageValue.
     */
    StorageValue<V> next();

    /**
     * 设定当前的下一个值.
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
     * 字段逻辑名称.
     * @return 名称.
     */
    String logicName();

    /**
     * 物理储存字段名称.
     * @return 名称.
     */
    String storageName();

    /**
     * 值所处于的位置.这个位置意为多个 StorageValue 才能组成一个 IValue 时的顺序定位.
     * @return 从0开始的位置.
     */
    int location();

    /**
     * 指定当前 value 所处于的位置序号.
     * @param location 目标序号,从0开始.
     * @return 定位后的位置.从0开始.
     */
    int locate(int location);

    /**
     * 储存的字段值.
     * @return
     */
    V value();

    /**
     * 当前的储存类型.
     * @return 类型.
     */
    StorageType type();


}