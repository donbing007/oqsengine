package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
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

    /**
     * 字段配置.
     */
    private FieldConfig config;

    /**
     * 构造一个使用默认配置的字段.
     * @param id 字段标识.
     * @param name 字段名称.
     * @param fieldType 字段类型.
     */
    public Field(long id, String name, FieldType fieldType) {
        this(id, name, fieldType, null);
    }

    /**
     * 构造一个独特配置的字段.
     * @param id 字段标识.
     * @param name 字段名称.
     * @param fieldType 字段类型.
     * @param config 字段配置.
     */
    public Field(long id, String name, FieldType fieldType, FieldConfig config) {
        this.id = id;
        this.name = name;
        this.fieldType = fieldType;

        if (config == null) {
            this.config = FieldConfig.build();
        } else {
            this.config = config;
        }
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

    @Override
    public FieldConfig config() {
        return this.config;
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
            fieldType == field.fieldType &&
            Objects.equals(config, field.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fieldType, config);
    }

    @Override
    public String toString() {
        return "Field{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", fieldType=" + fieldType +
            ", config=" + config +
            '}';
    }
}