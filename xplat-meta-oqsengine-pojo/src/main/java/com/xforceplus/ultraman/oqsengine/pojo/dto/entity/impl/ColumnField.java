package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.io.Serializable;
import java.util.Objects;

/**
 * a field as a column
 * only when column id and column name is same the column is same
 *
 * @author admin
 */
public class ColumnField implements IEntityField, Wrapped<IEntityField>, Serializable {

    private final IEntityField originField;

    private final IEntityClass originEntityClass;

    private final String name;

    private int index;

    public ColumnField(String name, IEntityField originField, IEntityClass originEntityClass) {

        Objects.requireNonNull(originField, "originField should not be null");
        //Objects.requireNonNull(originEntityClass, "originEntityClass should not be null");

        this.originField = originField;
        this.originEntityClass = originEntityClass;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public IEntityField originField() {
        return originField;
    }

    public IEntityClass originEntityClass() {
        return originEntityClass;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnField that = (ColumnField) o;
        return originField.equals(that.originField) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originField, name);
    }

    @Override
    public String toString() {
        return "ColumnField{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", originField=" + originField +
                '}';
    }


    @Override
    public IEntityField getOriginObject() {
        return originField;
    }
}
