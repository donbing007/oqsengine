package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

import java.util.StringJoiner;

/**
 * 枚举型字段储存策略.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 18:04
 * @since 1.8
 */
public class EnumStorageStrategy implements StorageStrategy {

    @Override
    public FieldType fieldType() {
        return FieldType.ENUM;
    }

    @Override
    public StorageType storageType() {
        return StorageType.STRING;
    }

    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue) {
        StorageValue point = storageValue;
        StringJoiner stringJoiner = new StringJoiner(EnumValue.DELIMITER);
        while (point != null) {
            stringJoiner.add(((String) point.value()).trim());

            point = point.next();
        }

        return new EnumValue(field, stringJoiner.toString());
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        String values = ((EnumValue) value).getValue();

        if (values.length() == 0) {
            throw new IllegalArgumentException("Unexpected enumeration values.");
        }


        StorageValue head = null;
        StorageValue point;
        for (String v : values.split(EnumValue.DELIMITER)) {
            point = new StringStorageValue(Long.toString(value.getField().id()), v, true);

            if (head == null) {
                head = point;
                head.locate(0);
            } else {
                head = head.stick(point);
            }
        }

        return head;
    }

    @Override
    public boolean isMultipleStorageValue() {
        return true;
    }
}
