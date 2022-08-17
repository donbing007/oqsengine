package com.xforceplus.ultraman.oqsengine.calculation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

/**
 * 计算字段重算框架.
 * 只做maintain阶段的工作.
 * 因为重算过程已经将实例计算完毕.
 */
public class JustMaintainCalculationImpl extends DefaultCalculationImpl {
    @Override
    public IEntity calculate(CalculationContext context) {
        return context.getFocusEntity();
    }
}
