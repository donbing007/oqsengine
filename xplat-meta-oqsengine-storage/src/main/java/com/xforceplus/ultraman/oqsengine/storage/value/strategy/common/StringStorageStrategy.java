package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.helper.AttachmentHelper;
import java.util.Optional;

/**
 * 逻辑类型为字符串的处理策略.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 17:51
 * @since 1.8
 */
public class StringStorageStrategy implements StorageStrategy {
    @Override
    public FieldType fieldType() {
        return FieldType.STRING;
    }

    @Override
    public StorageType storageType() {
        return StorageType.STRING;
    }

    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue, String attachemnt) {
        return new StringValue(field, (String) storageValue.value(), attachemnt);
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        StringStorageValue storageValue =
            new StringStorageValue(Long.toString(value.getField().id()), (String) value.getValue(), true);

        storageValue.notLocationAppend();

        AttachmentHelper.setStorageValueAttachemnt(value, storageValue);

        return storageValue;
    }

    @Override
    public StorageValue toEmptyStorageValue(IEntityField field, Optional<StorageValue<String>> attachment) {
        StringStorageValue storageValue = new StringStorageValue(Long.toString(field.id()), true);

        if (attachment.isPresent()) {
            storageValue.setAttachment(attachment.get());
        }

        return storageValue;
    }

    @Override
    public boolean isMultipleStorageValue() {
        return false;
    }
}
