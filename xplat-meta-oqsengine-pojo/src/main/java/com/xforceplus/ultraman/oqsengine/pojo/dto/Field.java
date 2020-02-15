package com.xforceplus.ultraman.oqsengine.pojo.dto;


import java.io.Serializable;
import java.util.Objects;

/**
 * 字段对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class Field implements Serializable {

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段类型
     */
    private String fieldType;

    public Field() {
    }

    public Field(String name, String fieldType) {
        this.name = name;
        this.fieldType = fieldType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Field)) return false;
        Field field = (Field) o;
        return Objects.equals(getName(), field.getName()) &&
                Objects.equals(getFieldType(), field.getFieldType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getFieldType());
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", fieldType='" + fieldType + '\'' +
                '}';
    }
}