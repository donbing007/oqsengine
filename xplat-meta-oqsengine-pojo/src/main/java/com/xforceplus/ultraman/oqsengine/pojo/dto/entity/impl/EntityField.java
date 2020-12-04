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
public class EntityField implements IEntityField, Serializable {

    /**
     * 表示主键字段.
     */
    public static final IEntityField ID_ENTITY_FIELD =
        new EntityField(0, "id", FieldType.LONG, FieldConfig.build().identifie(true));

    /**
     * 字段的标识.
     */
    private long id;

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字典中文名
     */
    private String cnName;

    /**
     * 字段类型
     */
    private FieldType fieldType;

    /**
     * 默认字典项
     */
    private String dictId;

    /**
     * 默认值 - 如果是字典，默认值位字典项的id信息
     */
    private String defaultValue;

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
    public EntityField(long id, String name, FieldType fieldType) {
        this(id, name, fieldType, null);
    }

    /**
     * 构造一个使用默认配置的字段.
     * @param id 字段标识.
     * @param name 字段名称.
     * @param fieldType 字段类型.
     */
    public EntityField(long id, String name, FieldType fieldType, FieldConfig config, String dictId, String defaultValue) {
        this(id, name, fieldType, config);
        this.dictId = dictId;
        this.defaultValue = defaultValue;
    }

    /**
     * 构造一个使用默认配置的字段.
     * @param id 字段标识.
     * @param name 字段名称.
     * @param fieldType 字段类型.
     */
    public EntityField(long id, String name, String cnName, FieldType fieldType, FieldConfig config, String dictId, String defaultValue) {
        this(id, name, fieldType, config);
        this.cnName = cnName;
        this.dictId = dictId;
        this.defaultValue = defaultValue;
    }

    /**
     * 构造一个独特配置的字段.
     * @param id 字段标识.
     * @param name 字段名称.
     * @param fieldType 字段类型.
     * @param config 字段配置.
     */
    public EntityField(long id, String name, FieldType fieldType, FieldConfig config) {
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
    public String cnName() { return this.cnName; }

    @Override
    public FieldType type() {
        return this.fieldType;
    }

    @Override
    public FieldConfig config() {
        return this.config;
    }

    @Override
    public String dictId() {
        return this.dictId;
    }

    @Override
    public String defaultValue() {
        return this.defaultValue;
    }

    public String getDictId() {
        return dictId;
    }

    public void setDictId(String dictId) {
        this.dictId = dictId;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getCnName() { return cnName; }

    public void setCnName(String cnName) { this.cnName = cnName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityField)) {
            return false;
        }
        EntityField entityField = (EntityField) o;
        return id == entityField.id &&
                Objects.equals(name, entityField.name) &&
                fieldType == entityField.fieldType &&
                Objects.equals(dictId, entityField.dictId) &&
                Objects.equals(defaultValue, entityField.defaultValue) &&
                Objects.equals(config, entityField.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fieldType, dictId, defaultValue, config);
    }

    @Override
    public String toString() {
        return "Field{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fieldType=" + fieldType +
                ", dictId='" + dictId + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", config=" + config +
                '}';
    }
}
