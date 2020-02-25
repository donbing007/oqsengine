package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

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

    /**
     * 字段是否可搜索
     */
    private boolean searchType;

    /**
     * 字段数据最大长度
     */
    private int maxSize;

    /**
     * 字段数据最小长度
     */
    private int mixSize;

    public Field() {
    }

    public Field(long id, String name, FieldType fieldType, boolean searchType, int maxSize, int mixSize) {
        this.id = id;
        this.name = name;
        this.fieldType = fieldType;
        this.searchType = searchType;
        this.maxSize = maxSize;
        this.mixSize = mixSize;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public boolean isSearchType() {
        return searchType;
    }

    public void setSearchType(boolean searchType) {
        this.searchType = searchType;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMixSize() {
        return mixSize;
    }

    public void setMixSize(int mixSize) {
        this.mixSize = mixSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Field)) return false;
        Field field = (Field) o;
        return getId() == field.getId() &&
                isSearchType() == field.isSearchType() &&
                getMaxSize() == field.getMaxSize() &&
                getMixSize() == field.getMixSize() &&
                Objects.equals(getName(), field.getName()) &&
                getFieldType() == field.getFieldType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getFieldType(), isSearchType(), getMaxSize(), getMixSize());
    }

    @Override
    public String toString() {
        return "Field{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fieldType=" + fieldType +
                ", searchType=" + searchType +
                ", maxSize=" + maxSize +
                ", mixSize=" + mixSize +
                '}';
    }

    @Override
    public long id() {
        return this.id;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public FieldType type() {
        return this.fieldType;
    }
}