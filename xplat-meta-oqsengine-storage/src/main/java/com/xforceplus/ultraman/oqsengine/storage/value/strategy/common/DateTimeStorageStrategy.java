package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 日期/时间的储存策略.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 18:05
 * @since 1.8
 */
public class DateTimeStorageStrategy extends LongStorageStrategy {

    @Override
    public FieldType fieldType() {
        return FieldType.DATETIME;
    }


    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue) {
        Instant instant = Instant.ofEpochMilli((long) storageValue.value());
        return new DateTimeValue(field, LocalDateTime.ofInstant(instant, DateTimeValue.ZONE_ID));
    }
}
