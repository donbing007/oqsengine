package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.fieldtype;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.ConditionValidation;

/**
 * 布尔的条件校验器.
 *
 * @author dongbin
 * @version 0.1 2020/2/29 16:34
 * @since 1.8
 */
public class BoolanConditionValidation implements ConditionValidation {
    @Override
    public boolean validate(Condition condition) {
        switch (condition.getOperator()) {
            case EQUALS:
            case NOT_EQUALS:
            case MULTIPLE_EQUALS:
                return true;
            default:
                return false;
        }
    }
}
