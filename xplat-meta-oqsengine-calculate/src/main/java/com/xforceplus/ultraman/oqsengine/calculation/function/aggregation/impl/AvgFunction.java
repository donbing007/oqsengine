package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.BigDecimalSummaryStatistics;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 平均值聚合.
 *
 * @className: AvgFunction
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/23 18:33
 */
public class AvgFunction implements AggregationFunction {

    @Override
    public Optional<IValue> excute(Optional<IValue> agg, ValueChange valueChange) {
        Optional<IValue> o = valueChange.getOldValue();
        Optional<IValue> n = valueChange.getNewValue();

        if (!(agg != null && o != null && n != null)) {
            return Optional.empty();
        }
        Optional<IValue> aggValue = Optional.of(agg.get().copy());
        if (agg.get() instanceof DecimalValue) {
            if (o.get() instanceof EmptyTypedValue || !o.isPresent()) {
                o = Optional.of(new DecimalValue(o.get().getField(), BigDecimal.ZERO));
            }
            if (n.get() instanceof EmptyTypedValue || !n.isPresent()) {
                n = Optional.of(new DecimalValue(n.get().getField(), BigDecimal.ZERO));
            }
            if (!agg.isPresent()) {
                aggValue = Optional.of(new DecimalValue(o.get().getField(), BigDecimal.ZERO));
            }
            BigDecimal temp = ((DecimalValue) agg.get()).getValue()
                .add(((DecimalValue) n.get()).getValue())
                .subtract(((DecimalValue) o.get()).getValue());
            aggValue.get().setStringValue(temp.toString());
            return Optional.of(aggValue.get());
        } else if (agg.get() instanceof LongValue) {
            if (o.get() instanceof EmptyTypedValue || !o.isPresent()) {
                o = Optional.of(new LongValue(o.get().getField(), 0L));
            }
            if (n.get() instanceof EmptyTypedValue || !n.isPresent()) {
                n = Optional.of(new LongValue(n.get().getField(), 0L));
            }
            if (!agg.isPresent()) {
                aggValue = Optional.of(new LongValue(n.get().getField(), 0L));
            }
            Long temp = agg.get().valueToLong() + n.get().valueToLong() - o.get().valueToLong();
            aggValue.get().setStringValue(temp.toString());
            return Optional.of(aggValue.get());
        }
        return Optional.empty();
    }

    @Override
    public Optional<IValue> init(Optional<IValue> agg, List<Optional<IValue>> values) {
        Optional<IValue> aggValue = Optional.of(agg.get().copy());
        if (agg.get() instanceof DecimalValue) {
            BigDecimalSummaryStatistics temp = values.stream().map(v -> ((DecimalValue) v.get()).getValue())
                .collect(BigDecimalSummaryStatistics.statistics());
            aggValue.get().setStringValue(temp.getAverage(MathContext.DECIMAL64).toString());
        } else if (agg.get() instanceof LongValue) {
            LongSummaryStatistics temp =
                values.stream().map(o -> o.get()).collect(Collectors.summarizingLong(IValue::valueToLong));
            aggValue.get().setStringValue(new DecimalFormat("0").format(temp.getAverage()));
        }
        return Optional.of(aggValue.get());
    }
}
