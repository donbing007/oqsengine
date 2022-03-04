package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

/**
 * 长整形逻辑字段的储存通用策略.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 17:40
 * @since 1.8
 */
public class LongStorageStrategy implements StorageStrategy {

    @Override
    public FieldType fieldType() {
        return FieldType.LONG;
    }

    @Override
    public StorageType storageType() {
        return StorageType.LONG;
    }

    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue, String attachemnt) {
        return new LongValue(field, (Long) storageValue.value(), attachemnt);
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        return new LongStorageValue(Long.toString(value.getField().id()), value.valueToLong(), true);
    }

    @Override
    public StorageValue toEmptyStorageValue(IEntityField field) {
        return new LongStorageValue(Long.toString(field.id()), 0, true);
    }

    @Override
    public boolean isMultipleStorageValue() {
        return false;
    }
}
