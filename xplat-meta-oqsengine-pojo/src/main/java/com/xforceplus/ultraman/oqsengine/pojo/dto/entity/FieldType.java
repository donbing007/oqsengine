package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

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

    UNKNOWN("Unknown", s -> false
            , StringValue::new),
    BOOLEAN("Boolean", s -> {
        try {
            Boolean.parseBoolean(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }, new String[]{"boolean"}
            , (f, v) ->
            new BooleanValue(f, Boolean.parseBoolean(v))),
    ENUM("Enum", new String[]{"enum"}
            , EnumValue::new),
    DATETIME("DateTime", s -> {
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
    }),
    LONG("Long", s -> {
        try {
            Long.parseLong(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
            , new String[]{"bigint", "long", "serialNo"}
            , (f, v) -> new LongValue(f, Long.parseLong(v))),

    STRING("String", new String[]{"string"}
            , StringValue::new),
    STRINGS("Strings", new String[]{"strings"}
            , StringsValue::new),
    DECIMAL("Decimal", s -> {
        try {
            new BigDecimal(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }, new String[]{"double", "decimal"}
            , (f, v) -> {
        int precision = Optional.ofNullable(f.config())
                .map(FieldConfig::getPrecision)
                .filter(x -> x > 0).orElse(1);
        return new DecimalValue(f, new BigDecimal(v)
                .setScale(precision, RoundingMode.HALF_UP));
    });

    private String type;

    private Predicate<String> tester;

    private String[] accepts;

    private BiFunction<IEntityField, String, IValue> iValueConverter;

    /**
     * @param type            field raw type
     * @param tester          test if a string value can be considered as this type
     * @param accepts         alias for this type
     * @param iValueConverter converter ivalue
     */
    FieldType(String type, Predicate<String> tester, String[] accepts, BiFunction<IEntityField, String, IValue> iValueConverter) {
        this.type = type;
        this.tester = tester;
        this.accepts = accepts;
        this.iValueConverter = iValueConverter;
    }

    FieldType(String type, Predicate<String> tester, BiFunction<IEntityField, String, IValue> iValueConverter) {
        this(type, tester, new String[]{}, iValueConverter);
    }

    FieldType(String type, String[] accepts, BiFunction<IEntityField, String, IValue> iValueConverter) {
        this(type, s -> true, accepts, iValueConverter);
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
