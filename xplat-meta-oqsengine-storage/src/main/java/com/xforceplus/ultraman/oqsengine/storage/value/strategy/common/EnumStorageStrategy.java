package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;

/**
 * 枚举型字段储存策略.
 * @author dongbin
 * @version 0.1 2020/3/4 18:04
 * @since 1.8
 */
public class EnumStorageStrategy extends StringStorageStrategy {

    @Override
    public FieldType fieldType() {
        return FieldType.ENUM;
    }

    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue) {
        return new EnumValue(field, (String) storageValue.value());
    }
}
