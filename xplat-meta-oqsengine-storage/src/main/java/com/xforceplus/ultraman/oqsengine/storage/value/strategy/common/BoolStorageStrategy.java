package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

/**
 * boolean 类型处理策略.
 * @author dongbin
 * @version 0.1 2020/3/4 18:01
 * @since 1.8
 */
public class BoolStorageStrategy extends LongStorageStrategy {

    @Override
    public FieldType fieldType() {
        return FieldType.BOOLEAN;
    }
}
