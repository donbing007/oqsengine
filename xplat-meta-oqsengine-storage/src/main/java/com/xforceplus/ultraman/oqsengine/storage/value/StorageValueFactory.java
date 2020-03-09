package com.xforceplus.ultraman.oqsengine.storage.value;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author dongbin
 * @version 0.1 2020/2/19 22:16
 * @since 1.8
 */
public class StorageValueFactory {

//    private static Map<FieldType, StorageType> FIELDTYPE_STORAGE_MAP;
//
//    static {
//        FIELDTYPE_STORAGE_MAP = new HashMap<>();
//        FIELDTYPE_STORAGE_MAP.put(FieldType.LONG, StorageType.LONG);
//        FIELDTYPE_STORAGE_MAP.put(FieldType.BOOLEAN, StorageType.LONG);
//        FIELDTYPE_STORAGE_MAP.put(FieldType.DATETIME, StorageType.LONG);
//        FIELDTYPE_STORAGE_MAP.put(FieldType.ENUM, StorageType.STRING);
//        FIELDTYPE_STORAGE_MAP.put(FieldType.STRING, StorageType.STRING);
//
//
//    }
//
//    public static StorageType findStorageType(FieldType type) {
//        return FIELDTYPE_STORAGE_MAP.get(type);
//    }

    /**
     * 构造一个储存 value 实例.
     * @param type 储存类型.
     * @param storageName 属性物理储存名称.
     * @param value 属性值.
     * @return 储存实例.
     */
    public static StorageValue buildStorageValue(StorageType type, String storageName, Object value) {
        switch(type) {
            case LONG: {
                if (Integer.class.isInstance(value)) {
                    return new LongStorageValue(storageName, ((Integer) value).longValue(), false);
                } else {
                    return new LongStorageValue(storageName, (Long)value, false);
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
