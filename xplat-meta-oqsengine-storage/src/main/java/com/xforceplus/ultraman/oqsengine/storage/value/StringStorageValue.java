package com.xforceplus.ultraman.oqsengine.storage.value;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;

/**
 * 字符串储存类型.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 13:34
 * @since 1.8
 */
public class StringStorageValue extends AbstractStorageValue<String> {

    /**
     * 构造一个空值表示.
     *
     * @param name 字段名称.
     * @param logicName true 名称为逻辑名称, false 名称为物理名称.
     */
    public StringStorageValue(String name, boolean logicName) {
        super(name, logicName, StorageType.STRING);
    }

    /**
     * 构造一个字符串的物理值.
     *
     * @param name 字段名称.
     * @param value 值.
     * @param logicName true 名称为逻辑名称, false 名称为物理名称.
     */
    public StringStorageValue(String name, String value, boolean logicName) {
        super(name, value, logicName);
    }
}
