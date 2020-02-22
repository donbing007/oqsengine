package com.xforceplus.ultraman.oqsengine.storage.helper;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dongbin
 * @version 0.1 2020/2/19 22:16
 * @since 1.8
 */
public class StorageTypeHelper {

    private static Map<FieldType, StorageType> FIELDTYPE_STORAGE_MAP;

    static {
        FIELDTYPE_STORAGE_MAP = new HashMap<>();
        FIELDTYPE_STORAGE_MAP.put(FieldType.LONG, StorageType.LONG);
        FIELDTYPE_STORAGE_MAP.put(FieldType.BOOLEAN, StorageType.LONG);
        FIELDTYPE_STORAGE_MAP.put(FieldType.DATATIME, StorageType.LONG);
        FIELDTYPE_STORAGE_MAP.put(FieldType.ENUM, StorageType.STRING);
        FIELDTYPE_STORAGE_MAP.put(FieldType.STRING, StorageType.STRING);
    }

    public static StorageType findStorageType(FieldType type) {
        return FIELDTYPE_STORAGE_MAP.get(type);
    }

}
