package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.reader.IEntityClassReader;
import com.xforceplus.ultraman.oqsengine.sdk.ValueUp;
import com.xforceplus.ultraman.oqsengine.sdk.service.HandleValueService;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.FieldOperationHandler;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.TriFunction;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator.FieldValidator;
import io.vavr.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * map to valueUp converter
 */
public class DefaultHandleValueService implements HandleValueService {

    private Logger logger = LoggerFactory.getLogger(HandleValueService.class);

    private final List<FieldOperationHandler> fieldOperationHandlers;

    private final List<FieldValidator<Object>> fieldValidators;

    public DefaultHandleValueService(List<FieldOperationHandler> fieldOperationHandlers, List<FieldValidator<Object>> fieldValidator) {

        this.fieldOperationHandlers = fieldOperationHandlers;
        this.fieldValidators = fieldValidator;
    }

    /**
     * TODO how to apply to any transformation
     * handle value framework
     *
     * @param entityClass
     * @param body
     */
    @Override
    public List<ValueUp> handlerValue(EntityClass entityClass, Map<String, Object> body, OperationType phase) {

        IEntityClassReader reader = new IEntityClassReader(entityClass);

        //get field from entityClass
        List<ValueUp> values = reader.zipValue(body)
                .map(tuple -> {

                    IEntityField field = tuple._1();
                    Object obj = tuple._2();

                    //pipeline and validate
                    Object value = pipeline(obj, field, phase);
                    List<Validation<String, Object>> validations = validate(field, value, phase);

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

    private Object pipeline(Object value, IEntityField field, OperationType phase) {

        return fieldOperationHandlers.stream()
                .sorted()
                .map(x -> (TriFunction) x)
                .reduce(TriFunction::andThen)
                .map(x -> x.apply(field, value, phase))
                .orElse(value);
    }

    private List<Validation<String, Object>> validate(IEntityField field, Object obj, OperationType phase) {

        return fieldValidators.stream()
                .map(x -> x.validate(field, obj, phase))
                .filter(Validation::isInvalid)
                .collect(Collectors.toList());
    }
}
