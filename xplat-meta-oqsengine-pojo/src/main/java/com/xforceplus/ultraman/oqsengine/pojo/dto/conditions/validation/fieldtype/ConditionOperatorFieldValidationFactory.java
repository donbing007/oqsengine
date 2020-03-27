package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.fieldtype;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.ConditionValidation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

import java.util.HashMap;
import java.util.Map;

/**
 * 条件连接符(like,=,!=,>,<,>=,<=) 可以使用的字段类型校验工厂.
 *
 * @author dongbin
 * @version 0.1 2020/2/29 16:19
 * @since 1.8
 */
public final class ConditionOperatorFieldValidationFactory {

    static Map<FieldType, ConditionValidation> VALIDATION_MAP;

    static {
        VALIDATION_MAP = new HashMap();
        VALIDATION_MAP.put(FieldType.BOOLEAN, new BoolanConditionValidation());
        VALIDATION_MAP.put(FieldType.DATETIME, new DateTimeConditionValidation());
        VALIDATION_MAP.put(FieldType.ENUM, new EnumConditionValidation());
        VALIDATION_MAP.put(FieldType.LONG, new LongConditionValidation());
        VALIDATION_MAP.put(FieldType.STRING, new StringConditionValidation());
        VALIDATION_MAP.put(FieldType.DECIMAL, new DecimalConditionValidation());
        VALIDATION_MAP.put(FieldType.STRINGS, new StringsConditionValidation());
    }

    // 不允许实例化.
    private ConditionOperatorFieldValidationFactory() {
    }

    public static ConditionValidation getValidation(FieldType type) {
        ConditionValidation validation = VALIDATION_MAP.get(type);
        if (validation == null) {
            throw new IllegalArgumentException("Unsupported validation of field types, this may be a BUG.");
        }

        return validation;
    }
}
