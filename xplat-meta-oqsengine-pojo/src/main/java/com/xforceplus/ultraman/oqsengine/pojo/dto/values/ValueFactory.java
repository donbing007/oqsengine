package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * IValue 构造工厂.
 * @author dongbin
 * @version 0.1 2020/2/18 20:43
 * @since 1.8
 */
public final class ValueFactory {


    /**
     * 构造一个储存值为数值型的字段.
     * @param field 属性.
     * @param value 储存值.
     * @return IValue 实例.
     */
    public static IValue buildValue(IEntityField field, long value) {
        switch(field.type()) {
            case LONG:
                return new LongValue(field, value);
            case DATETIME: {
                Instant instant = Instant.ofEpochMilli(value);
                return new DateTimeValue(field, LocalDateTime.ofInstant(instant, DateTimeValue.zoneId));
            }
            case BOOLEAN: {
                return new BooleanValue(field, value == 0? false : true);
            }
            default: {
                throw new UnsupportedOperationException("Unsupported operation, this should be a program BUG.");
            }

        }
    }

    /**
     * 构造一个储存值为字符串型的字段.
     * @param field 属性.
     * @param value 储存值.
     * @return IValue 实例.
     */
    public static IValue buildValue(IEntityField field, String value) {
        switch(field.type()) {
            case STRING:
                return new StringValue(field, value);
            case ENUM: {
                return new EnumValue(field, value);
            }
            default: {
                throw new UnsupportedOperationException("Unsupported operation, this should be a program BUG.");
            }

        }
    }

}
