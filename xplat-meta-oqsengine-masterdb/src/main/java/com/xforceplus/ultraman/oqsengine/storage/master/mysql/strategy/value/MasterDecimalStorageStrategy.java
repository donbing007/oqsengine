package com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.value;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.helper.AttachmentHelper;
import java.math.BigDecimal;

/**
 * master 中关于分数的储存策略.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 18:08
 * @since 1.8
 */
public class MasterDecimalStorageStrategy implements StorageStrategy {

    @Override
    public FieldType fieldType() {
        return FieldType.DECIMAL;
    }

    @Override
    public StorageType storageType() {
        return StorageType.STRING;
    }

    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue, String attachemnt) {
        return new DecimalValue(field, new BigDecimal((String) storageValue.value()), attachemnt);
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        String decValue = value.valueToString();

        // Ensure correct formatting.
        StringStorageValue storageValue;
        if (decValue.indexOf('.') < 0) {
            storageValue = new StringStorageValue(Long.toString(value.getField().id()), decValue + ".0", true);
        } else {
            storageValue = new StringStorageValue(Long.toString(value.getField().id()), decValue, true);
        }

        AttachmentHelper.setStorageValueAttachemnt(value, storageValue);

        return storageValue;
    }

    @Override
    public StorageValue toEmptyStorageValue(IEntityField field) {
        return new StringStorageValue(Long.toString(field.id()), "", true);
    }

    @Override
    public boolean isMultipleStorageValue() {
        return false;
    }

}
