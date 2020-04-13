package com.xforceplus.ultraman.oqsengine.sdk.record;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

/**
 * Record
 *
 * @author admin
 */
public class GeneralRecord implements Record {

    private Logger log = LoggerFactory.getLogger(GeneralRecord.class);

    final Object[] values;

    final IEntityField[] fields;

    public GeneralRecord(Collection<? extends IEntityField> fields) {

        int size = fields.size();

        this.fields = new IEntityField[fields.size()];
        this.values = new Object[size];

        int i = 0;
        for (IEntityField field : fields) {
            this.fields[i] = field;
            i++;
        }
    }

    /**
     * object cannot be null
     *
     * @param fieldName
     * @return
     */
    @Override
    public Optional<Object> get(String fieldName) {
        return field(fieldName).flatMap(this::get);
    }

    private Optional<IEntityField> field(String fieldName) {
        if (fieldName == null)
            return null;

        IEntityField fieldMatch = null;

        for (IEntityField f : fields)
            if (fieldName.equals(f.name()))
                if (fieldMatch == null)
                    fieldMatch = f;
                else
                    log.info("Ambiguous match found for "
                            + fieldName + ". Both " + fieldMatch + " and " + f + " match.");

        return Optional.ofNullable(fieldMatch);
    }

    /**
     * find
     *
     * @param field
     * @return
     */
    private final int indexOf(IEntityField field) {

        if (field != null) {
            int size = fields.length;

            for (int i = 0; i < size; i++)
                if (fields[i] == field)
                    return i;

            for (int i = 0; i < size; i++)
                if (fields[i].equals(field))
                    return i;
        }

        return -1;
    }

    @Override
    public Optional<Object> get(IEntityField field) {

        int index = indexOf(field);

        if (index > -1 && index < values.length) {
            return Optional.ofNullable(values[index]);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<IValue> getTypedValue(String fieldName) {
        return Optional.empty();
    }

    @Override
    public Optional<IValue> getTypedValue(IEntityField field) {
        return get(field)
                .flatMap(x -> field
                        .type()
                        .toTypedValue(field, x.toString()));
    }

    @Override
    public <T> Optional<T> get(String fieldName, Class<? extends T> type) {
        return field(fieldName)
                .flatMap(this::get)
                .map(x -> (T)x);
    }

    @Override
    public <T> Optional<T> get(IEntityField field, Class<? extends T> type) {
        return get(field)
                .map(x -> (T)x);
    }

    @Override
    public void set(String fieldName, Object t) {
        field(fieldName).ifPresent(x -> set(x, t));
    }

    @Override
    public void set(IEntityField field, Object t) {
        int index = indexOf(field);

        if (index > -1 && index < values.length) {
            values[index] = t;
        }

        log.warn("{} is not present", field.name());
    }

    @Override
    public void setTypedValue(IValue iValue) {

        if(iValue != null
                && iValue.getValue() != null
                && iValue.getField() != null){
            set(iValue.getField(), iValue.getValue());
        }
    }

    /**
     * TODO equals
     *
     * @param record
     * @return
     */
    @Override
    public int compareTo(Record record) {
        return 0;
    }
}
