package com.xforceplus.ultraman.oqsengine.pojo.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * desc :.
 * name : IValueUtils
 *
 * @author : xujia 2021/4/13
 * @since : 1.8
 */
public class IValueUtils {

    /**
     * serialize to String.
     */
    public static String serialize(IValue value) {

        if (value == null || value.getValue() == null) {
            return null;
        }

        if (value instanceof StringValue
            || value instanceof StringsValue
            || value instanceof DecimalValue
            || value instanceof EnumValue
            || value instanceof BooleanValue
        ) {
            return value.valueToString();
        } else if (value instanceof LongValue
            || value instanceof DateTimeValue) {
            return Long.toString(value.valueToLong());
        } else {
            return value.valueToString();
        }
    }

    /**
     * deserialize string to ivalue.
     */
    public static IValue deserialize(String rawValue, IEntityField entityField) {
        IValue retValue = null;

        if (rawValue == null) {
            return null;
        }

        switch (entityField.type()) {
            case STRING:
                retValue = new StringValue(entityField, rawValue);
                break;
            case STRINGS:
                retValue = new StringsValue(entityField, rawValue.split(","));
                break;
            case BOOLEAN:
                retValue = new BooleanValue(entityField, Boolean.parseBoolean(rawValue));
                break;
            case DATETIME:
                long timestamp = Long.parseLong(rawValue);
                LocalDateTime time =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        DateTimeValue.ZONE_ID);
                retValue = new DateTimeValue(entityField, time);
                break;
            case ENUM:
                retValue = new EnumValue(entityField, rawValue);
                break;
            case LONG:
                retValue = new LongValue(entityField, Long.parseLong(rawValue));
                break;
            case DECIMAL:
                retValue = new DecimalValue(entityField, new BigDecimal(rawValue));
                break;
            default:
        }

        return retValue;
    }
}
