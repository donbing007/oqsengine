package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.fieldtype;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.ConditionValidation;

/**
 * 字段类型为 FieldType.STRING 的当前条件连接符号校验.
 * @author dongbin
 * @version 0.1 2020/2/29 16:25
 * @since 1.8
 */
public class StringConditionValidation implements ConditionValidation {

    @Override
    public boolean validate(Condition condition) {
        switch(condition.getOperator()) {
            case EQUALS:
            case NOT_EQUALS:
            case LIKE: {
                return true;
            }
            default: return false;
        }
    }
}
