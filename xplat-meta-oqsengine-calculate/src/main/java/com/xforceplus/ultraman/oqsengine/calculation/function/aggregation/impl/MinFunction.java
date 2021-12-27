package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.BigDecimalSummaryStatistics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 最小值聚合.
 *
 * @className: MinFunction
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/23 18:33
 */
public class MinFunction implements AggregationFunction {

    @Override
    public Optional<IValue> excute(Optional<IValue> agg, Optional<IValue> o, Optional<IValue> n) {
        if (!(agg.isPresent() & o.isPresent() && n.isPresent())) {
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
                aggValue = Optional.of(new DecimalValue(n.get().getField(), BigDecimal.ZERO));
            }
            double temp = Math.min(((DecimalValue) n.get()).getValue().doubleValue(), ((DecimalValue) agg.get()).getValue().doubleValue());
            aggValue.get().setStringValue(String.valueOf(temp));
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
            long temp = Math.min(n.get().valueToLong(), agg.get().valueToLong());
            aggValue.get().setStringValue(String.valueOf(temp));
            return Optional.of(aggValue.get());
        } else if (agg.get() instanceof DateTimeValue) {
            if (o.get() instanceof EmptyTypedValue || !o.isPresent()) {
                o = Optional.of(new DateTimeValue(o.get().getField(), LocalDateTime.MAX));
            }
            if (n.get() instanceof EmptyTypedValue || !n.isPresent()) {
                n = Optional.of(new DateTimeValue(n.get().getField(), LocalDateTime.MAX));
            }
            if (!agg.isPresent()) {
                aggValue = Optional.of(new DateTimeValue(n.get().getField(), LocalDateTime.MAX));
            }
            long temp = Math.min(n.get().valueToLong(), agg.get().valueToLong());
            aggValue.get().setStringValue(String.valueOf(temp));
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
            aggValue.get().setStringValue(temp.getMin().toString());
        } else if (agg.get() instanceof LongValue) {
            LongSummaryStatistics temp = values.stream().map(o -> o.get()).collect(Collectors.summarizingLong(IValue::valueToLong));
            aggValue.get().setStringValue(String.valueOf(temp.getMin()));
        } else if (agg.get() instanceof DateTimeValue) {
            LongSummaryStatistics temp = values.stream().map(v -> v.get().valueToLong())
                    .collect(Collectors.summarizingLong(Long::longValue));
            aggValue.get().setStringValue(String.valueOf(temp.getMin()));
        }
        return Optional.of(aggValue.get());
    }

    @Override
    public Optional<Long> init(long agg, List<Long> values) {
        LongSummaryStatistics temp = values.stream().collect(Collectors.summarizingLong(Long::longValue));
        return Optional.of(temp.getMin());
    }

    @Override
    public Optional<BigDecimal> init(BigDecimal agg, List<BigDecimal> values) {
        BigDecimalSummaryStatistics temp = values.stream().collect(BigDecimalSummaryStatistics.statistics());
        return Optional.of(temp.getMin());
    }

    @Override
    public Optional<LocalDateTime> init(LocalDateTime agg, List<LocalDateTime> values) {
        ZoneOffset zone = ZoneOffset.of(ZoneOffset.systemDefault().getId());
        LongSummaryStatistics temp = values.stream().map(v -> v.toEpochSecond(zone))
                .collect(Collectors.summarizingLong(Long::longValue));
        return Optional.of(LocalDateTime.ofEpochSecond(temp.getMin(), 0, zone));
    }

}
