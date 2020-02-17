package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.core.metadata.IValue;

import java.time.LocalTime;
import java.util.Objects;

public class LocalTimeValue implements IValue<LocalTime> {
    private String name;
    private LocalTime value;
    @Override
    public String getName() {
        return null;
    }

    @Override
    public LocalTime getValue(String name) {
        return null;
    }

    @Override
    public LocalTime setValue(String name, LocalTime value) {
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

    public LocalTime getValue() {
        return value;
    }

    public void setValue(LocalTime value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalTimeValue)) return false;
        LocalTimeValue that = (LocalTimeValue) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }

    @Override
    public String toString() {
        return "LocalTimeValue{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
