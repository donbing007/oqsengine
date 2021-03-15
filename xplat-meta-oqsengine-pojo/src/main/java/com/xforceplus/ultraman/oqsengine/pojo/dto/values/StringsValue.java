package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author dongbin
 * @version 0.1 2020/3/27 18:24
 * @since 1.8
 */
public class StringsValue extends AbstractValue<String[]> {

    private final static String DELIMITER = ",";

    public StringsValue(IEntityField field, String ...value) {
        super(field, value);
    }

    @Override
    String[] fromString(String value) {
        return value == null ? null : value.split(DELIMITER);
    }

    @Override
    public long valueToLong() {
        throw new UnsupportedOperationException("A string cannot be represented by a number.");
    }

    @Override
    public IValue<String[]> shallowClone() {
        return new StringsValue(this.getField(), this.getValue());
    }

    @Override
    public String valueToString() {
        return String.join(DELIMITER, getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StringsValue)) {
            return false;
        }

        String[] thatValues = ((StringsValue) o).getValue();
        if (thatValues.length != this.getValue().length) {
            return false;
        }

        boolean found;
        for (String v : this.getValue()) {
            found = true;
            for (String tv : thatValues) {
                if (tv.equals(v)) {
                    // found,so next.
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getField(), getValue());
    }

    @Override
    public String toString() {
        return "StringValue{" +
            "field=" + getField() +
            ", value=" + Arrays.toString(this.getValue()) +
            '}';
    }
}
