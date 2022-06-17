package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.impl;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactoryImpl;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.FunctionStrategy;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Optional;

/**
 * 统计数量运算.
 *
 * @author wangzheng
 * @version 0.1 2021/08/23 17:52
 * @since 1.8
 */
public class CountFunctionStrategy implements FunctionStrategy {
    @Override
    public Optional<IValue> excute(
        Optional<IValue> currentValue,
        Optional<IValue> oldValue,
        Optional<IValue> newValue,
        CalculationContext context) {
        Optional<IValue> aggValue = Optional.of(currentValue.get().copy());
        Aggregation aggregation = ((Aggregation) context.getFocusField().config().getCalculation());
        AggregationFunction function =
            AggregationFunctionFactoryImpl.getAggregationFunction(aggregation.getAggregationType());
        return function.excute(aggValue, oldValue, newValue);
    }
}
