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
import java.util.Optional;

/**
 * 多字符串转换策略.
 *
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
    public IValue toLogicValue(IEntityField field, StorageValue storageValue, String attachemnt) {
        StorageValue point = storageValue;
        List<String> vs = new ArrayList();
        while (point != null) {
            vs.add(((String) point.value()).trim());

            point = point.next();
        }

        return new StringsValue(field, vs.toArray(new String[0]), attachemnt);
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        String[] values = ((StringsValue) value).getValue();

        if (values.length == 0) {
            throw new IllegalArgumentException("Unexpected enumeration values.");
        }


        StringStorageValue head = null;
        StringStorageValue point;
        for (String v : values) {
            point = new StringStorageValue(Long.toString(value.getField().id()), v, true);

            if (head == null) {
                head = point;
                head.locate(0);
            } else {
                head = (StringStorageValue) head.stick(point);
            }
        }

        Optional<String> attachment = value.getAttachment();
        if (attachment.isPresent()) {
            StringStorageValue attachemntStorageValue =
                new StringStorageValue(Long.toString(value.getField().id()), attachment.get(), true);
            head.setAttachment(attachemntStorageValue);
        }

        return head;
    }

    @Override
    public StorageValue toEmptyStorageValue(IEntityField field) {
        return new StringStorageValue(Long.toString(field.id()),  true);
    }

    @Override
    public boolean isMultipleStorageValue() {
        return true;
    }

    @Override
    public boolean isSortable() {
        return false;
    }
}
