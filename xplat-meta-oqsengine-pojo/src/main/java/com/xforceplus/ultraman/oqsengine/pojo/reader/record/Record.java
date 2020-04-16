package com.xforceplus.ultraman.oqsengine.pojo.reader.record;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import io.vavr.Tuple2;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 */
public interface Record extends Comparable<Record>  {

    Optional<Object> get(String fieldName);

    Optional<Object> get(IEntityField field);

    Optional<IValue> getTypedValue(String fieldName);

    Optional<IValue> getTypedValue(IEntityField field);

    <T> Optional<T> get(String fieldName, Class<? extends T> type);

    <T> Optional<T> get(IEntityField field, Class<? extends T> type);

    void set(String fieldName, Object t);

    void set(IEntityField field, Object t);

    void setTypedValue(IValue iValue);

    Stream<Tuple2<IEntityField, Object>> stream();

    Map<String, Object> toMap(Set<String> keys);
}
