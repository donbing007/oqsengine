package com.xforceplus.ultraman.oqsengine.storage.value;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;

/**
 * @author dongbin
 * @version 0.1 2020/2/19 22:16
 * @since 1.8
 */
public class StorageValueFactory {

    /**
     * 构造一个储存 value 实例.
     *
     * @param type        储存类型.
     * @param storageName 属性物理储存名称.
     * @param value       属性值.
     * @return 储存实例.
     */
    public static StorageValue buildStorageValue(StorageType type, String storageName, Object value) {
        switch (type) {
            case LONG: {
                if (Integer.class.isInstance(value)) {
                    return new LongStorageValue(storageName, ((Integer) value).longValue(), false);
                } else {
                    return new LongStorageValue(storageName, (Long) value, false);
                }
            }
            case STRING:
                return new StringStorageValue(storageName, (String) value, false);
            default:
                throw new IllegalStateException(
                    String.format("Unhandled storage type (%s), which may be a BUG.", type.name()));
        }
    }


}
