package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.core.metadata.IValue;

import java.util.Objects;

public class BooleanValue implements IValue<Boolean> {
    private String name;
    private Boolean value;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Boolean getValue(String name) {
        return null;
    }

    @Override
    public Boolean setValue(String name, Boolean value) {
        return null;
    }

    @Override
    public String valueToString() {
        return null;
    }

    @Override
    public Long valueToLong() {
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BooleanValue)) return false;
        BooleanValue that = (BooleanValue) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }

    @Override
    public String toString() {
        return "BooleanValue{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
