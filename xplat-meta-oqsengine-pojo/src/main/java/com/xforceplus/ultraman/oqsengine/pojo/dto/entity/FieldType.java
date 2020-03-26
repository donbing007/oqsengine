package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

/**
 * 字段类型枚举信息
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public enum FieldType {
    UNKNOWN("Unknown"),
    BOOLEAN("Boolean"),
    ENUM("Enum"),
    DATETIME("DateTime"),
    LONG("Long"),
    STRING("String"),
    DECIMAL("Decimal");

    private String type;

    FieldType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
