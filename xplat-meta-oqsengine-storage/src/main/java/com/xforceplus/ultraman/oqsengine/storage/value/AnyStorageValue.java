package com.xforceplus.ultraman.oqsengine.storage.value;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;

/**
 * 任意对象储存.
 * @author dongbin
 * @version 0.1 2020/3/5 00:40
 * @since 1.8
 */
public class AnyStorageValue extends AbstractStorageValue<Object> {
    /**
     * 使用物理字段名和名构造一个储存值实例.
     *
     * @param name      字段名称.
     * @param value     储存的值.
     * @param logicName true 逻辑名称,false 物理储存名称.
     */
    public AnyStorageValue(String name, Object value, boolean logicName) {
        super(name, value, logicName);
    }

    @Override
    public StorageType type() {
        return StorageType.UNKNOWN;
    }

    public static StorageValue getInstance(String storageName) {
        return new AnyStorageValue(storageName, null, false);
    }
}
