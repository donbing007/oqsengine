package com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.control.Validation;

public class RequiredValidator implements FieldValidator<Object> {

    @Override
    public Validation<String, Object> validate(IEntityField field, Object obj) {
        return  isRequired(field) && obj == null
                ? Validation.invalid(String.format("Required field %s must be present", field.name()))
                : Validation.valid(obj);
    }

    private boolean isRequired(IEntityField field){
        return field.config() != null && field.config().isRequired();
    }
}
