package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.core.enums.FieldType;
import com.xforceplus.ultraman.oqsengine.core.metadata.IValue;

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
     * @param name 属性名称.
     * @param value 储存值.
     * @param type 逻辑类型.
     * @return IValue 实例.
     */
    public static IValue buildValue(Long id, String name, long value, FieldType type) {
        switch(type) {
            case LONG:
                return new LongValue(id, name, value);
            case DATATIME: {
                Instant instant = Instant.ofEpochMilli(value);
                return new DateTimeValue(id, name, LocalDateTime.ofInstant(instant, DateTimeValue.zoneId));
            }
            case BOOLEAN: {
                return new BooleanValue(id, name, value == 0? false : true);
            }
            default: {
                throw new UnsupportedOperationException("Unsupported operation, this should be a program BUG.");
            }

        }
    }

    /**
     * 构造一个储存值为字符串型的字段.
     * @param name 属性名称.
     * @param value 储存值.
     * @param type 逻辑类型.
     * @return IValue 实例.
     */
    public static IValue buildValue(Long id, String name, String value, FieldType type) {
        switch(type) {
            case LONG:
                return new StringValue(id, name, value);
            case ENUM: {
                return new EnumValue(id, name, value);
            }
            default: {
                throw new UnsupportedOperationException("Unsupported operation, this should be a program BUG.");
            }

        }
    }
}
