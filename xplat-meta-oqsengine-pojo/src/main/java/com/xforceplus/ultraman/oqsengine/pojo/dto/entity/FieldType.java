package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * TODO Unknown currently is not used
 * 字段类型枚举信息
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public enum FieldType {

    UNKNOWN("Unknown", s -> false),
    BOOLEAN("Boolean", s -> {
        try {
            Boolean.parseBoolean(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }, new String[]{"boolean"}),
    ENUM("Enum", new String[]{"enum"}),
    DATETIME("DateTime", s -> {
        try {
            Instant.ofEpochMilli(Long.parseLong(s));
            return true;
        } catch (Exception e) {
            return false;
        }
    }, new String[]{"timestamp"}),
    LONG("Long", s -> {
        try {
            Long.parseLong(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }, new String[]{"bigint", "long", "serialNo"}),
    STRING("String", new String[]{"string"}),
    STRINGS("Strings", new String[]{"strings"}),
    DECIMAL("Decimal", s -> {
        try {
            new BigDecimal(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }, new String[]{"double"});

    private String type;

    private Predicate<String> tester;

    private String[] accepts;

    FieldType(String type, Predicate<String> tester, String[] accepts) {
        this.type = type;
        this.tester = tester;
        this.accepts = accepts;
    }

    FieldType(String type, Predicate<String> tester) {
        this(type, tester, new String[]{});
    }

    FieldType(String type){
        this(type, s -> true, new String[]{});
    }

    FieldType(String type, String[] accepts){
        this(type, s -> true, accepts);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean canParseFrom(String input){
        return tester.test(input);
    }

    public boolean accept(String rawType){
        return Stream.of(accepts).anyMatch(x -> x.equalsIgnoreCase(rawType));
    }

    public static FieldType fromRawType(String rawType){

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
