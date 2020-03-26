package com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.control.Validation;
import org.apache.commons.lang3.BooleanUtils;

/**
 * check type
 */
public class TypeCheckValidator implements FieldValidator<Object> {



    @Override
    public Validation<String, Object> validate(IEntityField field, Object obj) {

        if(obj != null) {
            field.type();

            if (field.config().isSplittable()) {

            } else {

            }
        }
        return Validation.valid(obj);
    }


    /**
     * this check may be not check with runtime type
     *
     *
     * @param type
     * @param obj
     * @return
     */
    private boolean checkType(FieldType type, Object obj){
        switch (type){
            case BOOLEAN:
                return obj instanceof Boolean || "true".equalsIgnoreCase(obj)

            case ENUM:
                return obj instanceof String || obj instanceof Integer;
            case DATETIME:
                return obj instanceof Long || Long.getLong(obj.toString())
        }
    }
}
