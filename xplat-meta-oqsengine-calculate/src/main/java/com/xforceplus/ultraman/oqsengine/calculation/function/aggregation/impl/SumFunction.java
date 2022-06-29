package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.BigDecimalSummaryStatistics;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.able.CalculationsAble;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 求和聚合.
 *
 * @className: SumFunction
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/23 18:32
 */
public class SumFunction implements AggregationFunction {
    @Override
    public Optional<IValue> excute(Optional<IValue> agg, ValueChange valueChange) {
        Optional<IValue> o = valueChange.getOldValue();
        Optional<IValue> n = valueChange.getNewValue();
        if (!(agg.isPresent() & o.isPresent() && n.isPresent())) {
            return Optional.empty();
        }

        IValue aggCopyValue = agg.get().copy();

        IValue oldValue;
        if (o.get() instanceof EmptyTypedValue) {
            oldValue = IValueUtils.zero(o.get().getField());
        } else {
            oldValue = o.get();
        }
        IValue newValue;

        if (n.get() instanceof EmptyTypedValue) {
            newValue = IValueUtils.zero(n.get().getField());
        } else {
            newValue = n.get();
        }

        CalculationsAble ca = (CalculationsAble) aggCopyValue;
        return Optional.of((IValue) ca.plus(newValue).subtract(oldValue));
    }

    @Override
    public Optional<IValue> init(Optional<IValue> agg, List<Optional<IValue>> values) {
        Optional<IValue> aggValue = Optional.of(agg.get().copy());
        if (agg.get() instanceof DecimalValue) {
            BigDecimalSummaryStatistics temp = values.stream().map(v -> ((DecimalValue) v.get()).getValue())
                .collect(BigDecimalSummaryStatistics.statistics());
            aggValue.get().setStringValue(temp.getSum().toString());
        } else if (agg.get() instanceof LongValue) {
            LongSummaryStatistics temp =
                values.stream().map(o -> o.get()).collect(Collectors.summarizingLong(IValue::valueToLong));
            aggValue.get().setStringValue(String.valueOf(temp.getSum()));
        }
        return Optional.of(aggValue.get());
    }
}
