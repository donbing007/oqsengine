package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.core.metadata.IValue;

import java.util.Objects;

public class LocalDate implements IValue<LocalDate> {
    private String name;
    private LocalDate value;
    @Override
    public String getName() {
        return null;
    }

    @Override
    public LocalDate getValue(String name) {
        return null;
    }

    @Override
    public LocalDate setValue(String name, LocalDate value) {
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

    public LocalDate getValue() {
        return value;
    }

    public void setValue(LocalDate value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalDate)) return false;
        LocalDate localDate = (LocalDate) o;
        return Objects.equals(getName(), localDate.getName()) &&
                Objects.equals(getValue(), localDate.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }

    @Override
    public String toString() {
        return "LocalDate{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
