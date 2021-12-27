package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

/**
 * 表示无法支持默认查询策略,唯一的作用即是上报异常.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 17:31
 * @since 1.8
 */
public class UnsupportStorageStrategy implements StorageStrategy {

    @Override
    public FieldType fieldType() {
        return FieldType.UNKNOWN;
    }

    @Override
    public StorageType storageType() {
        return StorageType.UNKNOWN;
    }

    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue, String attachemnt) {
        throw new UnsupportedOperationException("Unknown logical attribute that cannot be handled.");
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        throw new UnsupportedOperationException("Unknown logical attribute that cannot be handled.");
    }

    @Override
    public StorageValue toEmptyStorageValue(IEntityField field) {
        throw new UnsupportedOperationException("Unknown logical attribute that cannot be handled.");
    }

    @Override
    public boolean isMultipleStorageValue() {
        throw new UnsupportedOperationException("Unknown logical attribute that cannot be handled.");
    }

    @Override
    public boolean isSortable() {
        throw new UnsupportedOperationException("Unknown logical attribute that cannot be handled.");
    }
}
