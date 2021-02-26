package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;


import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(value = "id")
    private long id;

    /**
     * 字段名称
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * 字典中文名
     */
    @JsonProperty(value = "cnName")
    private String cnName;

    /**
     * 字段类型
     */
    @JsonProperty(value = "type")
    private FieldType fieldType;

    /**
     * 默认字典项
     */
    @JsonProperty(value = "dictId")
    private String dictId;

    /**
     * 默认值 - 如果是字典，默认值位字典项的id信息
     */
    @JsonProperty(value = "defaultValue")
    private String defaultValue;

    /**
     * 字段配置.
     */
    @JsonProperty(value = "config")
    private FieldConfig config;

    public EntityField() {
    }

    /**
     * 构造一个使用默认配置的字段.
     * @param id 字段标识.
     * @param name 字段名称.
     * @param fieldType 字段类型.
     * @deprecated 使用builder构造.
     */
    @Deprecated
    public EntityField(long id, String name, FieldType fieldType) {
        this(id, name, fieldType, null);
    }

    /**
     * 构造一个使用默认配置的字段.
     * @param id 字段标识.
     * @param name 字段名称.
     * @param fieldType 字段类型.
     * @deprecated 使用builder构造.
     */
    @Deprecated
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
     * @deprecated 使用builder构造.
     */
    @Deprecated
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
     * @deprecated 使用builder构造.
     */
    @Deprecated
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

    /**
     * Builder
     */
    public static final class Builder {
        private long id;
        private String name;
        private String cnName;
        private FieldType fieldType;
        private String dictId;
        private String defaultValue;
        private FieldConfig config;

        private Builder() {
        }

        public static Builder anEntityField() {
            return new Builder();
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withCnName(String cnName) {
            this.cnName = cnName;
            return this;
        }

        public Builder withFieldType(FieldType fieldType) {
            this.fieldType = fieldType;
            return this;
        }

        public Builder withDictId(String dictId) {
            this.dictId = dictId;
            return this;
        }

        public Builder withDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder withConfig(FieldConfig config) {
            this.config = config;
            return this;
        }

        public EntityField build() {
            EntityField entityField = new EntityField();
            entityField.id = this.id;
            entityField.name = this.name;
            entityField.cnName = this.cnName;
            entityField.fieldType = this.fieldType;
            entityField.dictId = this.dictId;
            entityField.defaultValue = this.defaultValue;
            entityField.config = this.config;
            return entityField;
        }
    }
}

