package com.xforceplus.ultraman.oqsengine.pojo.reader.record;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Record
 * <p>
 * field and Row
 *
 * @author admin
 */
public class GeneralRecord implements Record {

    private Logger log = LoggerFactory.getLogger(GeneralRecord.class);

    private final Object[] values;

    private final IEntityField[] fields;

    private final Boolean isEmpty;


    public static Record empty() {
        return new GeneralRecord();
    }

    private GeneralRecord() {
        isEmpty = true;
        values = null;
        fields = null;
    }

    ;

    public GeneralRecord(Collection<? extends IEntityField> fields) {

        int size = fields.size();

        this.fields = new IEntityField[fields.size()];
        this.values = new Object[size];

        int i = 0;
        for (IEntityField field : fields) {
            this.fields[i] = field;
            i++;
        }

        isEmpty = false;
    }

    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
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

    @Override
    public Object get(int index) {
        if (index >= values.length) {
            log.error("OutOfBound exception for visit index:{} and return null!", index);
            return null;
        }
        return values[index];
    }

    private Optional<IEntityField> field(String fieldName) {
        if (fieldName == null) {
            return null;
        }

        IEntityField fieldMatch = null;

        for (IEntityField f : fields) {
            if (fieldName.equals(f.name())) {
                if (fieldMatch == null) {
                    fieldMatch = f;
                } else {
                    log.info("Ambiguous match found for "
                            + fieldName + ". Both " + fieldMatch + " and " + f + " match.");
                }
            }
        }

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

            for (int i = 0; i < size; i++) {
                if (fields[i] == field) {
                    return i;
                }
            }

            for (int i = 0; i < size; i++) {
                if (fields[i].equals(field)) {
                    return i;
                }
            }
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
        return field(fieldName).flatMap(this::getTypedValue);
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
                .map(x -> (T) x);
    }

    @Override
    public <T> Optional<T> get(IEntityField field, Class<? extends T> type) {
        return get(field)
                .map(x -> (T) x);
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
        } else {
            log.warn("{} is not present", field.name());
        }
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        map.forEach(this::set);
    }

    @Override
    public void setTypedValue(IValue iValue) {

        if (iValue != null
                && iValue.getValue() != null
                && iValue.getField() != null) {
            set(iValue.getField(), iValue.getValue());
        }
    }

    @Override
    public Stream<Tuple2<IEntityField, Object>> stream() {
        return IntStream.range(0, fields.length).mapToObj(i -> Tuple.of(fields[i], values[i]));
    }

    @Override
    public Stream<Tuple2<IEntityField, Object>> stream(Set<String> filterName) {
        return IntStream.range(0, fields.length)
                .mapToObj(i -> {
                    String name = fields[i].name();
                    if (filterName != null && !filterName.isEmpty()) {
                        if (filterName.contains(name)) {
                            return Tuple.of(fields[i], values[i]);
                        }
                        return null;
                    } else {
                        return Tuple.of(fields[i], values[i]);
                    }
                }).filter(Objects::nonNull);
    }

    @Override
    public Map<String, Object> toMap(Set<String> filterName) {

        Map<String, Object> map = new HashMap<>(values.length + 1);

        IntStream.range(0, values.length)
                .forEach(i -> {

                    String name = fields[i].name();
                    if (filterName != null && !filterName.isEmpty()) {
                        if (filterName.contains(name)) {
                            map.put(name, values[i]);
                        }
                    } else {
                        map.put(name, values[i]);
                    }

                });

        //make up map id and make sure id is long
        map.put("id", id);

        return map;
    }

    @Override
    public Map<String, Object> toNestedMap(Set<String> filterName) {

        Map<String, Object> map = new HashMap<>(values.length);

        IntStream.range(0, values.length)
                .forEach(i -> {
                    String name = fields[i].name();
                    Boolean insert = false;
                    if (filterName != null && !filterName.isEmpty()) {
                        if (filterName.contains(name)) {
                            insert = true;
                        }
                    } else {
                        insert = true;
                    }

                    if (insert) {
                        //insert
                        if (name.contains(".")) {
                            String[] names = name.split("\\.");

                            Map currentMap = map;

                            for (int index = 0; index < names.length - 1; index++) {
                                String seg = names[index];
                                Object segMap = currentMap.get(seg);
                                if (segMap == null) {
                                    Map<String, Object> innerMap = new HashMap<>();
                                    currentMap.put(seg, innerMap);
                                    currentMap = innerMap;
                                } else if (segMap instanceof Map) {
                                    currentMap = (Map) segMap;
                                }
                            }

                            currentMap.put(names[names.length - 1], values[i]);

                        } else {
                            map.put(name, values[i]);
                        }
                    }
                });

        return map;

    }

    @Override
    public Boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public Boolean nonEmpty() {
        return !isEmpty;
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
