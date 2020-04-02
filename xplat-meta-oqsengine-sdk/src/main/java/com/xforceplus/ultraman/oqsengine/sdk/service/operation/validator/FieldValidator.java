package com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;
import io.vavr.control.Validation;
import org.springframework.util.StringUtils;


/**
 * field validator for type T
 *
 * @param <T>
 * @author admin
 */
public interface FieldValidator<T> {

    Validation<String, T> validate(IEntityField field, T obj, OperationType phase);

    default boolean isSplittable(IEntityField field) {
        return field.config() != null &&
            field.config().isSplittable() &&
            !StringUtils.isEmpty(field.config().getDelimiter());

    }
}
