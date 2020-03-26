package com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.control.Validation;

/**
 * regx validator
 */
public class RegxValidator implements FieldValidator<Object> {

    @Override
    public Validation<String, Object> validate(IEntityField field, Object obj) {
        return null;
    }
}
