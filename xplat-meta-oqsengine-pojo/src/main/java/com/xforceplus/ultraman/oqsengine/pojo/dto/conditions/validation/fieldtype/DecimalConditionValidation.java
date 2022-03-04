package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.fieldtype;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;

/**
 * 大浮点数校验.
 *
 * @author dongbin
 * @version 0.1 2020/3/6 11:25
 * @since 1.8
 */
public class DecimalConditionValidation extends LongConditionValidation {

    @Override
    public boolean validate(Condition condition) {
        switch (condition.getOperator()) {
            case EQUALS:
            case NOT_EQUALS:
            case GREATER_THAN:
            case LESS_THAN:
            case LESS_THAN_EQUALS:
            case GREATER_THAN_EQUALS:
            case IS_NULL:
            case IS_NOT_NULL:
                return true;
            default:
                return false;
        }
    }
}
