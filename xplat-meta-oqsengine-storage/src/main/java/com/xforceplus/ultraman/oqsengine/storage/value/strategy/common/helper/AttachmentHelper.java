package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.helper;

import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.value.AbstractStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import java.util.Optional;

/**
 * 附件助手.
 *
 * @author dongbin
 * @version 0.1 2022/4/8 16:50
 * @since 1.8
 */
public final class AttachmentHelper {

    /**
     * 设置物理值附件.
     *
     * @param value 逻辑值.
     * @param storageValue 物理值.
     */
    public static void setStorageValueAttachemnt(IValue value, AbstractStorageValue storageValue) {

        Optional<String> attachment = value.getAttachment();
        if (attachment.isPresent()) {
            StringStorageValue attachemntStorageValue =
                new StringStorageValue(value.getField().idString(), attachment.get(), true);
            storageValue.setAttachment(attachemntStorageValue);
        }
    }
}
