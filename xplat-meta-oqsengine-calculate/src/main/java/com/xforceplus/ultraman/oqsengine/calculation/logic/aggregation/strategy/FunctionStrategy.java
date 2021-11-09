package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
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
     * @param agg  聚合字段.
     * @param o 被聚合字段旧值.
     * @param n  被聚合新值.
     * @param context  聚合上下文信息.
     * @return 计算后的内容.
     */
    Optional<IValue> excute(Optional<IValue> agg, Optional<IValue> o, Optional<IValue> n, CalculationContext context);
}