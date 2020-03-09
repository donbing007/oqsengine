package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

/**
 * 日期/时间的储存策略.
 * @author dongbin
 * @version 0.1 2020/3/4 18:05
 * @since 1.8
 */
public class DateTimeStorageStrategy extends LongStorageStrategy {

    @Override
    public FieldType fieldType() {
        return FieldType.DATETIME;
    }
}
