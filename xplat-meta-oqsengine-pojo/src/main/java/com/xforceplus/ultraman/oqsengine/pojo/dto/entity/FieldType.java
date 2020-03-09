package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

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
