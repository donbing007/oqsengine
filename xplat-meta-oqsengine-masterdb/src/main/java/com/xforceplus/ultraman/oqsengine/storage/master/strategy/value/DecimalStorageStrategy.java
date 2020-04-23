package com.xforceplus.ultraman.oqsengine.storage.master.strategy.value;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.StringStorageStrategy;

import java.math.BigDecimal;

/**
 * master 中关于分数的储存策略.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 18:08
 * @since 1.8
 */
public class DecimalStorageStrategy extends StringStorageStrategy {
    @Override
    public FieldType fieldType() {
        return FieldType.DECIMAL;
    }

    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue) {
        return new DecimalValue(field, new BigDecimal((String) storageValue.value()));
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        return new StringStorageValue(Long.toString(value.getField().id()), value.getValue().toString(), true);
    }
}
