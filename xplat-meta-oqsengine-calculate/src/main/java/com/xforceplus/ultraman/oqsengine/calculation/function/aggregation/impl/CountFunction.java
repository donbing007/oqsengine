package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.able.CalculationsAble;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 统计总数聚合.
 *
 * @className: CountFunction
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/23 18:33
 */
public class CountFunction implements AggregationFunction {

    @Override
    public Optional<IValue> excute(Optional<IValue> agg, ValueChange valueChange) {
        Optional<IValue> o = valueChange.getOldValue();
        Optional<IValue> n = valueChange.getNewValue();

        if (!n.isPresent()) {
            return Optional.of(agg.get());
        }

        IValue aggCopyValue = agg.get().copy();

        IValue<Long> oldValue;
        IValue<Long> newValue;
        if (!o.isPresent() || o.get() instanceof EmptyTypedValue) {
            oldValue = new LongValue(o.get().getField(), 0L);
        } else {
            oldValue = o.get();
        }

        if (!n.isPresent() || n.get() instanceof EmptyTypedValue) {
            newValue = new LongValue(n.get().getField(), 0L);
        } else {
            newValue = n.get();
        }

        if (!(oldValue.valueToLong() == 0) && newValue.valueToLong() == 0) {

            aggCopyValue = (IValue) ((CalculationsAble) aggCopyValue).decrement();

        } else if (oldValue.getValue() == 0 && newValue.getValue() != 0) {

            aggCopyValue = (IValue) ((CalculationsAble) aggCopyValue).increment();

        }

        return Optional.of(aggCopyValue);
    }

    @Override
    public Optional<IValue> init(Optional<IValue> agg, List<Optional<IValue>> values) {
        Optional<IValue> aggValue = Optional.of(agg.get().copy());
        if (agg.get() instanceof LongValue) {
            LongSummaryStatistics temp =
                values.stream().map(o -> o.get()).collect(Collectors.summarizingLong(IValue::valueToLong));
            aggValue.get().setStringValue(String.valueOf(temp.getCount()));
        }
        return Optional.of(aggValue.get());
    }

}
