package com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;
import io.vavr.control.Validation;

/**
 * consist field validator
 */
public interface ConsistFieldValidator<T> extends FieldValidator<T>{

    @Override
    default Validation<String, T> validate(IEntityField field, T obj, OperationType phase) {
        return validate(field, obj);
    }

    Validation<String, T> validate(IEntityField field, T obj);

}
