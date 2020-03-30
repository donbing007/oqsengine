package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * 多字符串转换策略.
 * @author dongbin
 * @version 0.1 2020/3/27 18:27
 * @since 1.8
 */
public class StringsStorageStrategy implements StorageStrategy {
    @Override
    public FieldType fieldType() {
        return FieldType.STRINGS;
    }

    @Override
    public StorageType storageType() {
        return StorageType.STRING;
    }

    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue) {
        StorageValue point = storageValue;
        List<String> vs = new ArrayList();
        while (point != null) {
            vs.add(((String) point.value()).trim());

            point = point.next();
        }

        return new StringsValue(field, vs.toArray(new String[0]));
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        String[] values = ((StringsValue) value).getValue();

        if (values.length == 0) {
            throw new IllegalArgumentException("Unexpected enumeration values.");
        }


        StorageValue head = null;
        StorageValue point;
        for (String v : values) {
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