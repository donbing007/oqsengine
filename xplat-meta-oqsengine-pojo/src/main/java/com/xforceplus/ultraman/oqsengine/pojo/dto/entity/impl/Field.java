package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.io.Serializable;
import java.util.Objects;

/**
 * 字段对象.
 *
 * @author wangzheng
 * @version 0.1 2020/2/13 15:30
 * @since 1.8
 */
public class Field implements IEntityField, Serializable {

    /**
     * 字段的标识.
     */
    private long id;

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

    public Field(long id, String name, FieldType fieldType) {
        this.id = id;
        this.name = name;
        this.fieldType = fieldType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Field)) {
            return false;
        }
        Field field = (Field) o;
        return id == field.id &&
            Objects.equals(name, field.name) &&
            fieldType == field.fieldType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fieldType);
    }

    @Override
    public String toString() {
        return "Field{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", fieldType=" + fieldType +
            '}';
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public FieldType type() {
        return fieldType;
    }
}