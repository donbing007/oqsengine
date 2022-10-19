package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.ConditionValidation;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作符条件校验器.
 *
 * @author dongbin
 * @version 0.1 2022/10/19 17:39
 * @since 1.8
 */
public class ConditionOperationValidationFactory {

    static final ConditionValidation DEFAULT_VALIDATION = new DefaultOperationConditionValidation();
    static Map<ConditionOperator, ConditionValidation> VALIDATION_MAP;

    static {
        VALIDATION_MAP = new HashMap();
    }

    // 不允许实例化.
    private ConditionOperationValidationFactory() {
    }

    /**
     * 获得校验实例.
     *
     * @param operator 字段类型.
     * @return 校验实例.
     */
    public static ConditionValidation getValidation(ConditionOperator operator) {
        ConditionValidation validation = VALIDATION_MAP.get(operator);

        if (validation == null) {
            validation = DEFAULT_VALIDATION;
        }

        return validation;
    }
}
