package com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;
import io.vavr.control.Validation;

/**
 * required value
 *
 * @author admin
 */
public class RequiredValidator implements FieldValidator<Object> {

    private boolean isRequired(IEntityField field) {
        return field.config() != null && field.config().isRequired();
    }

    @Override
    public Validation<String, Object> validate(IEntityField field, Object obj, OperationType phase) {
        if(phase != OperationType.UPDATE){
            return isRequired(field) && obj == null
                    ? Validation.invalid(String.format("Required field %s must be present", field.name()))
                    : Validation.valid(obj);
        }

        return Validation.valid(obj);
    }
}
