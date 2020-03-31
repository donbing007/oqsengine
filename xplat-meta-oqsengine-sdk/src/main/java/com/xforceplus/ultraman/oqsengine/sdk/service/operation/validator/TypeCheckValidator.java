package com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.control.Validation;
import org.springframework.util.StringUtils;

import java.util.stream.Stream;

/**
 * TODO
 * from field domain
 * check type
 */
public class TypeCheckValidator implements ConsistFieldValidator<Object> {

    @Override
    public Validation<String, Object> validate(IEntityField field, Object obj) {

        if (obj != null) {
            if (field.config().isSplittable() &&
                    !StringUtils.isEmpty(field.config().getDelimiter())) {
                String value = obj.toString();
                String[] terms = value.split(field.config().getDelimiter());

                return Stream.of(terms)
                        .allMatch(field.type()::canParseFrom) ?
                        Validation.valid(obj) :
                        Validation.invalid(String.format("%s is not satisfied to type %s", obj, field.type()));
            } else {
                return checkType(field.type(), obj) ?
                        Validation.valid(obj) :
                        Validation.invalid(String.format("%s is not satisfied to type %s", obj, field.type()));

            }
        }
        return Validation.valid(obj);
    }

    /**
     * this check may be not check with runtime type
     *
     * @param type
     * @param obj
     * @return
     */
    private boolean checkType(FieldType type, Object obj) {

        return type.canParseFrom(obj.toString());
    }
}
