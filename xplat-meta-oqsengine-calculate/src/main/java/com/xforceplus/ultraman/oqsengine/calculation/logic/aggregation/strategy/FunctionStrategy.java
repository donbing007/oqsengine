package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Optional;

/**
 * 函数计算策略.
 *
 * @author wangzheng.
 * @version 0.1 2021/08/23 17:52.
 * @since 1.8.
 */
public interface FunctionStrategy {

    /**
     * 聚合公式执行方法.
     *
     * @param currentValue 聚合字段本身的当前值.
     * @param valueChange  被聚合对象目标值的改变.
     * @param context      聚合上下文信息.
     * @return 计算后的内容.
     */
    Optional<IValue> excute(Optional<IValue> currentValue, ValueChange valueChange, CalculationContext context);
}
