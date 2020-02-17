package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.core.metadata.IValue;

import java.util.Date;
import java.util.Objects;

public class LongValue implements IValue<Long> {
    private String name;
    private Long value;
    @Override
    public String getName() {
        return null;
    }

    @Override
    public Long getValue(String name) {
        return null;
    }

    @Override
    public Long setValue(String name, Long value) {
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

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LongValue)) return false;
        LongValue longValue = (LongValue) o;
        return Objects.equals(getName(), longValue.getName()) &&
                Objects.equals(getValue(), longValue.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }

    @Override
    public String toString() {
        return "LongValue{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
