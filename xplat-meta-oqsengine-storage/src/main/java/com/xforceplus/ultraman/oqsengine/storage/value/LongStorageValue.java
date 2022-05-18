package com.xforceplus.ultraman.oqsengine.storage.value;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;

/**
 * 长整形的储存类值类型.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 13:31
 * @since 1.8
 */
public class LongStorageValue extends AbstractStorageValue<Long> {

    /**
     * 构造一个空值表示.
     *
     * @param name 字段名称.
     * @param logicName true 名称为逻辑名称, false 名称为物理名称.
     */
    public LongStorageValue(String name, boolean logicName) {
        super(name, logicName, StorageType.LONG);
    }

    /**
     * 构造一个数字的物理值.
     *
     * @param name 字段名称.
     * @param value 值.
     * @param logicName true 名称为逻辑名称, false 名称为物理名称.
     */
    public LongStorageValue(String name, long value, boolean logicName) {
        super(name, value, logicName);
    }
}
