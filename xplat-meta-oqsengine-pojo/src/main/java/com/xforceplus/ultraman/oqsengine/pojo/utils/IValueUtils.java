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
import java.util.Date;

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

    /**
     * 根据field与object转换IValue.
     */
    public static IValue<?> toIValue(IEntityField field, Object result) {
        try {
            switch (field.type()) {
                case BOOLEAN: {
                    return new BooleanValue(field, (Boolean) result);
                }
                case ENUM: {
                    return new EnumValue(field, (String) result);
                }
                case DATETIME: {
                    if (result instanceof Date) {
                        return new DateTimeValue(field, TimeUtils.convert((Date) result));
                    } else if (result instanceof LocalDateTime) {
                        return new DateTimeValue(field, (LocalDateTime) result);
                    }
                    return new DateTimeValue(field, TimeUtils.convert((Long) result));
                }
                case LONG: {
                    if (result instanceof Integer) {
                        result = ((Integer) result).longValue();
                    }
                    return new LongValue(field, (Long) result);
                }
                case STRING: {
                    return new StringValue(field, (String) result);
                }
                case STRINGS: {
                    return new StringsValue(field, (String[]) result);
                }
                case DECIMAL: {
                    BigDecimal r;
                    if (field.config().getPrecision() > 0) {
                        Scale scale = Scale.getInstance(field.config().scale());
                        if (!scale.equals(Scale.UN_KNOW)) {
                            r = ((BigDecimal) result).setScale(field.config().getPrecision(), scale.getMode());
                        } else {
                            r = (BigDecimal) result;
                        }
                    } else {
                        r = (BigDecimal) result;
                    }
                    return new DecimalValue(field, r);
                }
                default: {
                    throw new IllegalArgumentException("unknown field type.");
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("toIValue failed, message [%s]", e.getMessage()));
        }
    }


    /**
     * scal enum class.
     */
    public enum Scale {
        UN_KNOW(0, BigDecimal.ROUND_UNNECESSARY),
        ROUNDING(1, BigDecimal.ROUND_HALF_UP),
        CUT_OUT(2, BigDecimal.ROUND_DOWN);

        private int scale;
        private int mode;

        Scale(int scale, int mode) {
            this.scale = scale;
            this.mode = mode;
        }

        public int getScale() {
            return scale;
        }

        public int getMode() {
            return mode;
        }

        /**
         * get instance.
         *
         * @return scale instance.
         */
        public static Scale getInstance(int v) {
            for (Scale s : Scale.values()) {
                if (s.scale == v) {
                    return s;
                }
            }
            return Scale.UN_KNOW;
        }
    }
}
