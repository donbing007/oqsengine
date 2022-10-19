package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.operation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation.ConditionValidation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

/**
 * 默认的条件校验.<br>
 * 不允许出现空的右值.
 * <pre>
 *     c1 = 2  允许
 *     c1 = "" 不允许
 * </pre>
 *
 * @author dongbin
 * @version 0.1 2022/10/19 17:41
 * @since 1.8
 */
public class DefaultOperationConditionValidation implements ConditionValidation {

    @Override
    public boolean validate(Condition condition) {
        IValue[] values = condition.getValues();

        if (values == null || values.length == 0) {
            return false;
        }

        int failCount = 0;
        for (IValue v : values) {
            if (v.getValue() == null || v.getValue().toString().isEmpty()) {
                failCount++;
            }
        }

        // 表示所有值都不符合,触发空右值检查.
        if (failCount == values.length) {
            return false;
        }

        return true;
    }
}
