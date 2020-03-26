package com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.control.Validation;

/**
 * validate on field
 */
public interface FieldValidator<T> {

    Validation<String, T> validate(IEntityField field, T obj);
}
