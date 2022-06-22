package com.xforceplus.ultraman.oqsengine.pojo.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :.
 * name : IValueUtils
 *
 * @author : xujia 2021/4/13
 * @since : 1.8
 */
public class IValueUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(IValueUtils.class);

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
     * deserialize string to Condition.
     */
    public static Condition deserializeCondition(String rawValue, ConditionOperator operator, IEntityField entityField) {
        if (operator.equals(ConditionOperator.MULTIPLE_EQUALS)) {
            String[] rawValues = rawValue.split(",");
            IValue[] values = new IValue[rawValues.length];
            for (int i = 0; i < rawValues.length; i++) {
                values[i] = deserialize(rawValues[i], entityField);
            }
            return new Condition(entityField, operator, values);
        }
        return new Condition(entityField, operator, deserialize(rawValue, entityField));
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
            LOGGER.debug("raw : [{}], fieldId : [{}], config : [{}]]", result, field.id(), field.config().toString());
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
                    LOGGER.debug("in decimal, raw : [{}], fieldId : [{}], precision : [{}], scale : [{}]]",
                        result, field.id(), field.config().getPrecision(), field.config().scale());

                    BigDecimal r;
                    if (field.config().getPrecision() > 0) {
                        Scale scale = Scale.getInstance(field.config().scale());
                        if (!scale.equals(Scale.UN_KNOW)) {
                            r = toBigDecimal(result, field.config().getPrecision(), scale.getMode());
                        } else {
                            r = toBigDecimal(result, null, null);
                        }
                    } else {
                        r = toBigDecimal(result, null, null);
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

    private static BigDecimal toBigDecimal(Object value, Integer precision, Integer model) {
        BigDecimal toValue = null;

        if (value instanceof String) {
            toValue = new BigDecimal((String) value);
        } else if (value instanceof Long) {
            toValue = BigDecimal.valueOf((Long) value);
        } else if (value instanceof Integer) {
            toValue = BigDecimal.valueOf((Integer) value);
        } else if (value instanceof Double) {
            toValue = BigDecimal.valueOf((Double) value);
        } else if (value instanceof Float) {
            toValue = BigDecimal.valueOf((Float) value);
        } else if (value instanceof BigDecimal) {
            toValue = (BigDecimal) value;
        } else {
            throw new IllegalArgumentException(
                String.format("bigDecimal un-support type[%s], value[%s]", value.getClass(), value));
        }

        if (null != precision && null != model) {
            return toValue.setScale(precision, RoundingMode.valueOf(model));
        }
        return toValue;
    }

    /**
     * 获取指定数字类型的最大值.
     *
     * @param field 目标字段.
     * @return 最大值.
     */
    public static IValue max(IEntityField field) {
        switch (field.type()) {
            case DECIMAL:
                return new DecimalValue(field, new BigDecimal(Long.MAX_VALUE));
            case LONG:
                return new LongValue(field, Long.MAX_VALUE);
            case DATETIME:
                return new DateTimeValue(field, LocalDateTime.MAX);
            default: {
                throw new IllegalArgumentException(
                    String.format("The current type(%s) has no maximum value.", field.type().name()));
            }
        }
    }

    /**
     * 获取指定数字类型的最小值.
     *
     * @param field 目标字段.
     * @return 最小值.
     */
    public static IValue min(IEntityField field) {
        switch (field.type()) {
            case DECIMAL:
                return new DecimalValue(field, new BigDecimal(Long.MIN_VALUE));
            case LONG:
                return new LongValue(field, Long.MIN_VALUE);
            case DATETIME:
                return new DateTimeValue(field, LocalDateTime.MIN);
            default: {
                throw new IllegalArgumentException(
                    String.format("The current type(%s) has no maximum value.", field.type().name()));
            }
        }
    }

    /**
     * 获取指定数字类型的0值表示.
     * 不包含时间类型.
     *
     * @param field 目标字段.
     * @return 0值.
     */
    public static IValue zero(IEntityField field) {
        switch (field.type()) {
            case DECIMAL:
                return new DecimalValue(field, BigDecimal.ZERO);
            case LONG:
                return new LongValue(field, 0L);
            default: {
                throw new IllegalArgumentException(
                    String.format("The current type(%s) has no maximum value.", field.type().name()));
            }
        }
    }


    /**
     * scal enum class.
     */
    public enum Scale {
        UN_KNOW(0, BigDecimal.ROUND_UNNECESSARY),
        ROUND_HALF_UP(1, BigDecimal.ROUND_HALF_UP),
        ROUND_DOWN(2, BigDecimal.ROUND_DOWN);

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
