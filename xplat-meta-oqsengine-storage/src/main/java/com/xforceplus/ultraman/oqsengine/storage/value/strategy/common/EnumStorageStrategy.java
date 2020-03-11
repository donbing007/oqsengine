package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

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
}
