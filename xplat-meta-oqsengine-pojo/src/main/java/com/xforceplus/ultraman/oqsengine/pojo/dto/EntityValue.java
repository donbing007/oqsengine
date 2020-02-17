package com.xforceplus.ultraman.oqsengine.pojo.dto;

import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityValue;
import com.xforceplus.ultraman.oqsengine.core.metadata.IValue;

import java.util.List;
import java.util.Objects;

public class EntityValue implements IEntityValue {
    /**
     * 元数据boId
     */
    private Long id;

    /**
     * Entity的值集合
     */
    private List<IValue> values;

    @Override
    public Long id() {
        return null;
    }

    @Override
    public IValue getValue(String fieldName) {
        return null;
    }

    @Override
    public IEntityValue setValue(IValue value,String fieldType) {
        return null;
    }

    @Override
    public List<IValue> values() {
        return null;
    }

    @Override
    public IEntityValue setValues(List<IValue> values) {
        return null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<IValue> getValues() {
        return values;
    }

    public EntityValue(Long id, List<IValue> values) {
        this.id = id;
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityValue)) return false;
        EntityValue that = (EntityValue) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getValues(), that.getValues());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getValues());
    }
}
