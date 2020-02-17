package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.core.metadata.IValue;

import java.util.Objects;

public class EnumValue implements IValue<String> {
    private String name;
    private String value;
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getValue(String name) {
        return null;
    }

    @Override
    public String setValue(String name, String value) {
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnumValue)) return false;
        EnumValue enumValue = (EnumValue) o;
        return Objects.equals(getName(), enumValue.getName()) &&
                Objects.equals(getValue(), enumValue.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }

    @Override
    public String toString() {
        return "EnumValue{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
