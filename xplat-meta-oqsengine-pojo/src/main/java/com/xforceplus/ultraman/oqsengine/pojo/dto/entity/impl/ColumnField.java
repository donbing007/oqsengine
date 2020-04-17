package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.io.Serializable;
import java.util.Objects;

/**
 * a field as a column
 * @author admin
 */
public class ColumnField implements IEntityField, Serializable {

    private final IEntityField originField;

    private final String name;

    private final int index;

    public ColumnField(int index, String name, IEntityField originField){
        Objects.requireNonNull(originField, "field should not be null");
        this.originField = originField;
        this.index = index;
        this.name = name;
    }

    @Override
    public long id() {
        return originField.id();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String cnName() {
        return originField.cnName();
    }

    @Override
    public FieldType type() {
        return originField.type();
    }

    @Override
    public FieldConfig config() {
        return originField.config();
    }

    @Override
    public String dictId() {
        return originField.dictId();
    }

    @Override
    public String defaultValue() {
        return originField.defaultValue();
    }

    public IEntityField originField(){
        return originField;
    }

    @Override
    public String toString() {
        return "ColumnField{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", originField=" + originField +
                '}';
    }
}
