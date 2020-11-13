package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * TODO Unknown currently is not used
 * 字段类型枚举信息
 *
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public enum FieldType {

    /**
     * unknown field
     */
    UNKNOWN("Unknown", s -> false
            , StringValue::new),

    /**
     * boolean
     */
    BOOLEAN("Boolean", Boolean.class, s -> {
        try {
            Boolean.parseBoolean(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }, new String[]{"boolean"}
            , (f, v) ->
            new BooleanValue(f, Boolean.parseBoolean(v))
            , (v1, v2) -> {
        Boolean value1 = ((BooleanValue) v1).getValue();
        Boolean value2 = ((BooleanValue) v2).getValue();
        return Boolean.compare(value1, value2);
    }),
    /**
     * enum
     */
    ENUM("Enum", new String[]{"enum"}
            , EnumValue::new),

    /**
     * datetime
     */
    DATETIME("DateTime", Long.class, s -> {
        try {
            Instant.ofEpochMilli(Long.parseLong(s));
            return true;
        } catch (Exception e) {
            return false;
        }
    }, new String[]{"timestamp"}
            , (f, v) -> {
        Instant instant = Instant.ofEpochMilli(Long.parseLong(v));
        return new DateTimeValue(f
                , LocalDateTime.ofInstant(instant, DateTimeValue.zoneId));
    }, (v1, v2) -> {
        LocalDateTime value1 = ((DateTimeValue) v1).getValue();
        LocalDateTime value2 = ((DateTimeValue) v2).getValue();
        return value1.compareTo(value2);
    }),
    /**
     * Long
     */
    LONG("Long", Long.class, s -> {
        try {
            Long.parseLong(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
            , new String[]{"bigint", "long", "serialNo"}
            , (f, v) -> new LongValue(f, Long.parseLong(v))
            , (v1, v2) -> {

        Long value1 = ((LongValue) v1).getValue();
        Long value2 = ((LongValue) v2).getValue();
        return Long.compare(value1, value2);
    }),
    /**
     * String
     */
    STRING("String", new String[]{"string"}
            , StringValue::new),
    /**
     * strings
     */
    STRINGS("Strings",
            String.class,
            s -> {
                try {
                    s.trim().split(",");
                    return true;
                } catch (Exception e) {
                    return false;
                }
            },
            new String[]{"strings"}
            , (x, str) -> {
        return new StringsValue(x, str.trim().split(","));
    },
            (v1, v2) -> 0
    ),
    /**
     * decimal
     */
    DECIMAL("Decimal", BigDecimal.class, s -> {
        try {
            new BigDecimal(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }, new String[]{"double", "decimal"}
            , (f, v) -> {
        //default precision is 2
        int precision = Optional.ofNullable(f.config())
                .map(FieldConfig::getPrecision)
                .filter(x -> x > 0).orElse(2);
        return new DecimalValue(f, new BigDecimal(v)
                .setScale(precision, RoundingMode.HALF_UP));
    },
            (v1, v2) -> {
                BigDecimal value1 = ((DecimalValue) v1).getValue();
                BigDecimal value2 = ((DecimalValue) v2).getValue();
                return value1.compareTo(value2);
            }
    );

    private String type;

    private Predicate<String> tester;

    private String[] accepts;

    private Class javaType;

    private BiFunction<IEntityField, String, IValue> iValueConverter;

    private BiFunction<IValue, IValue, Integer> comparator;

    /**
     * @param type            field raw type
     * @param tester          test if a string value can be considered as this type
     * @param accepts         alias for this type
     * @param iValueConverter converter ivalue
     * @param javaType        type used by calcite
     */
    FieldType(String type, Class javaType, Predicate<String> tester, String[] accepts
            , BiFunction<IEntityField, String, IValue> iValueConverter
            , BiFunction<IValue, IValue, Integer> comparator
    ) {
        this.type = type;
        this.tester = tester;
        this.accepts = accepts;
        this.iValueConverter = iValueConverter;
        this.javaType = javaType;
        this.comparator = comparator;
    }

    FieldType(String type, Predicate<String> tester, BiFunction<IEntityField, String, IValue> iValueConverter) {
        this(type, String.class, tester, new String[]{}, iValueConverter, (v1, v2) -> v1.valueToString().compareTo(v2.valueToString()));
    }

    FieldType(String type, String[] accepts, BiFunction<IEntityField, String, IValue> iValueConverter) {
        this(type, String.class, s -> true, accepts, iValueConverter, (v1, v2) -> v1.valueToString().compareTo(v2.valueToString()));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean canParseFrom(String input) {
        return tester.test(input);
    }

    public int compare(String o1, String o2) {
        if (comparator == null) {
            return 0;
        }

        IEntityField field = new EntityField(-1, "dummy", this);

        IValue ivalueA = this.iValueConverter.apply(field, o1);
        IValue ivalueB = this.iValueConverter.apply(field, o2);

        return this.comparator.apply(ivalueA, ivalueB);
    }

    public Optional<IValue> toTypedValue(IEntityField entityField, String value) {
        Objects.requireNonNull(value, "value值不能为空");
        Objects.requireNonNull(entityField, "field值不能为空");

        if (this.tester.test(value)) {
            return Optional.ofNullable(iValueConverter.apply(entityField, value));
        } else {
            return Optional.empty();
        }
    }

    public boolean accept(String rawType) {
        return Stream.of(accepts).anyMatch(x -> x.equalsIgnoreCase(rawType));
    }

    public Class getJavaType() {
        return javaType;
    }

    public static FieldType fromRawType(String rawType) {

        try {
            return FieldType.valueOf(rawType.toUpperCase());
        } catch (Exception ex) {
            //to
        }

        return Stream.of(FieldType.values())
                .filter(x -> x.accept(rawType))
                .findFirst().orElse(FieldType.STRING);
    }
}
