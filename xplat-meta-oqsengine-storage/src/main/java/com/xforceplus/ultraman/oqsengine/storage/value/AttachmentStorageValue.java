package com.xforceplus.ultraman.oqsengine.storage.value;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;

/**
 * 附件的储存类型.
 *
 * @author dongbin
 * @version 0.1 2021/11/25 18:26
 * @since 1.8
 */
public class AttachmentStorageValue extends StringStorageValue {

    public AttachmentStorageValue(String name, String value, boolean logicName) {
        super(name, value, logicName);
    }

    @Override
    public StorageType type() {
        return StorageType.STRING;
    }
}
