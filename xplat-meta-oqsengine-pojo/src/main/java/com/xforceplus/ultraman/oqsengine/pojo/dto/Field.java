package com.xforceplus.ultraman.oqsengine.pojo.dto;


import com.xforceplus.ultraman.oqsengine.core.enums.FieldType;

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
     * 字段ID
     */
    private Long id;

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段类型
     */
    private FieldType fieldType;

    public Field() {
    }

    public Field(Long id, String name, FieldType fieldType) {
        this.id = id;
        this.name = name;
        this.fieldType = fieldType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Field)) return false;
        Field field = (Field) o;
        return Objects.equals(getId(), field.getId()) &&
                Objects.equals(getName(), field.getName()) &&
                getFieldType() == field.getFieldType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getFieldType());
    }

    @Override
    public String toString() {
        return "Field{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fieldType=" + fieldType +
                '}';
    }
}