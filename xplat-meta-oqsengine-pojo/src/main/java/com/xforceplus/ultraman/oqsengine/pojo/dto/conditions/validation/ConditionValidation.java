package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.validation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;

/**
 * 条件验证器.
 * @author dongbin
 * @version 0.1 2020/2/29 16:16
 * @since 1.8
 */
public interface ConditionValidation {

    /**
     * 验证条件.
     * @param condition 目标条件.
     */
    boolean validate(Condition condition);
}
