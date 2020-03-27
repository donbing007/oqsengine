package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.sdk.ValueUp;
import com.xforceplus.ultraman.oqsengine.sdk.service.HandleValueService;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.FieldOperationHandler;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.TriFunction;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator.FieldValidator;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Validation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * handle value to valueUp converter
 */
public class DefaultHandleValueService implements HandleValueService {

    @Autowired
    private List<FieldOperationHandler> fieldOperationHandlers;

    @Autowired
    private List<FieldValidator<Object>> fieldValidators;

    /**
     * TODO how to apply to any transformation
     * handle value framework
     *
     * @param entityClass
     * @param body
     */
    @Override
    public List<ValueUp> handlerValue(EntityClass entityClass, Map<String, Object> body, String phase) {

        //get field from entityClass
        List<ValueUp> values = zipValue(entityClass, body)
                .map(tuple -> {

                    //Field Object
                    // This is a shape
                    //TODO object toString is ok?
                    IEntityField field = tuple._1();
                    Object obj = tuple._2();

                    //pipeline and validate
                    Object value = pipeline(obj, field, phase);
                    List<Validation<String, Object>> validations = validate(field, value);

                    if (!validations.isEmpty()) {
                        throw new RuntimeException(validations.stream()
                                .map(Validation::getError)
                                .collect(Collectors.joining(",")));
                    }

                    if (value != null) {
                        return ValueUp.newBuilder()
                                .setFieldId(field.id())
                                .setFieldType(field.type().getType())
                                .setValue(value.toString())
                                .build();
                    } else {
                        return null;
                    }

                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return values;
    }

    /**
     * zip two
     *
     * @param entityClass
     * @param body
     * @return
     */
    private Stream<Tuple2<IEntityField, Object>> zipValue(IEntityClass entityClass, Map<String, Object> body) {
        Stream<IEntityField> fields = entityClass.fields().stream();
        Stream<IEntityField> relationFields = entityClass.relations().stream().map(Relation::getEntityField);
        Stream<IEntityField> parentFields = Optional.ofNullable(entityClass.extendEntityClass())
                .map(IEntityClass::fields)
                .orElseGet(Collections::emptyList)
                .stream();

        return Stream.concat(parentFields, Stream.concat(fields, relationFields))
                .map(x -> Tuple.of(x, body.get(x.name())));
    }

    private Object pipeline(Object value, IEntityField field, String phase) {

        return fieldOperationHandlers.stream()
                .sorted()
                .map(x -> (TriFunction) x)
                .reduce(TriFunction::andThen)
                .get()
                .apply(field, value, phase);
    }

    private List<Validation<String, Object>> validate(IEntityField field, Object obj) {

        return fieldValidators.stream()
                .map(x -> x.validate(field, obj))
                .filter(Validation::isInvalid)
                .collect(Collectors.toList());
    }
}
