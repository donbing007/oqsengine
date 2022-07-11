package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.impl;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactoryImpl;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.FunctionStrategy;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Optional;

/**
 * Created by justin.xu on 07/2022.
 *
 * @since 1.8
 */
public class CollectFunctionStrategy implements FunctionStrategy {
    @Override
    public Optional<IValue> excute(Optional<IValue> currentValue, ValueChange valueChange, CalculationContext context) {

        Optional<IValue> aggValue = Optional.of(currentValue.get().copy());
        Aggregation aggregation = ((Aggregation) context.getFocusField().config().getCalculation());
        AggregationFunction function =
                AggregationFunctionFactoryImpl.getAggregationFunction(aggregation.getAggregationType());
        return function.excute(aggValue, valueChange);
    }
}
