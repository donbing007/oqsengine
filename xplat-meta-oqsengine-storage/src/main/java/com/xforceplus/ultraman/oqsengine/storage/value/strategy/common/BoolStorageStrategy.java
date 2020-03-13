package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;

/**
 * boolean 类型处理策略.
 * @author dongbin
 * @version 0.1 2020/3/4 18:01
 * @since 1.8
 */
public class BoolStorageStrategy extends LongStorageStrategy {

    @Override
    public FieldType fieldType() {
        return FieldType.BOOLEAN;
    }


    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue) {
        return new BooleanValue(field, convert(storageValue));
    }

    private boolean convert(StorageValue storageValue) {
        long value = (long) storageValue.value();
        return value > 0 ? true : false;
    }
}
