package com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.control.Validation;
import org.springframework.util.StringUtils;


/**
 * field validator for type T
 * @author admin
 * @param <T>
 */
public interface FieldValidator<T> {

    Validation<String, T> validate(IEntityField field, T obj);

    default boolean isSplittable(IEntityField field) {
        return field.config() != null &&
                field.config().isSplittable() &&
                !StringUtils.isEmpty(field.config().getDelimiter());

    }
}
