package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.BigDecimalSummaryStatistics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 最大值聚合.
 *
 * @className: MaxFunction
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/23 18:33
 */
public class MaxFunction implements AggregationFunction {

    @Override
    public Optional<IValue> excute(Optional<IValue> agg, Optional<IValue> o, Optional<IValue> n) {
        if (!(agg.isPresent() & o.isPresent() && n.isPresent())) {
            return Optional.empty();
        }
        if (agg.get() instanceof DecimalValue) {
            double temp = Math.max(Math.max(((DecimalValue) n.get()).getValue().doubleValue(),
                    ((DecimalValue) o.get()).getValue().doubleValue()), ((DecimalValue) agg.get()).getValue().doubleValue());
            agg.get().setStringValue(String.valueOf(temp));
            return Optional.of(agg.get());
        } else if (agg.get() instanceof LongValue) {
            long temp = Math.max(Math.max(n.get().valueToLong(), o.get().valueToLong()), agg.get().valueToLong());
            agg.get().setStringValue(String.valueOf(temp));
            return Optional.of(agg.get());
        } else if (agg.get() instanceof DateTimeValue) {
            ZoneOffset zone = ZoneOffset.of(ZoneOffset.systemDefault().getId());
            ((DateTimeValue) n.get()).getValue().toEpochSecond(zone);
            long temp = Math.max(Math.max(((DateTimeValue) n.get()).getValue().toEpochSecond(zone),
                    ((DateTimeValue) o.get()).getValue().toEpochSecond(zone)),
                    ((DateTimeValue) agg.get()).getValue().toEpochSecond(zone));
            agg.get().setStringValue(String.valueOf(temp));
            return Optional.of(agg.get());
        }
        return Optional.empty();
    }

    @Override
    public Optional<IValue> init(Optional<IValue> agg, List<Optional<IValue>> values) {
        if (agg.get() instanceof DecimalValue) {
            BigDecimalSummaryStatistics temp = values.stream().map(v -> ((DecimalValue) v.get()).getValue())
                    .collect(BigDecimalSummaryStatistics.statistics());
            agg.get().setStringValue(temp.getMax().toString());
        } else if (agg.get() instanceof LongValue) {
            LongSummaryStatistics temp = values.stream().map(o -> o.get()).collect(Collectors.summarizingLong(IValue::valueToLong));
            agg.get().setStringValue(String.valueOf(temp.getMax()));
        } else if (agg.get() instanceof DateTimeValue) {
            ZoneOffset zone = ZoneOffset.of(ZoneOffset.systemDefault().getId());
            LongSummaryStatistics temp = values.stream().map(v -> ((DateTimeValue) v.get()).getValue().toEpochSecond(zone))
                    .collect(Collectors.summarizingLong(Long::longValue));
            agg.get().setStringValue(String.valueOf(temp.getMax()));
        }
        return Optional.of(agg.get());
    }

    @Override
    public Optional<Long> init(long agg, List<Long> values) {
        LongSummaryStatistics temp = values.stream().collect(Collectors.summarizingLong(Long::longValue));
        return Optional.of(temp.getMax());
    }

    @Override
    public Optional<BigDecimal> init(BigDecimal agg, List<BigDecimal> values) {
        BigDecimalSummaryStatistics temp = values.stream().collect(BigDecimalSummaryStatistics.statistics());
        return Optional.of(temp.getMax());
    }

    @Override
    public Optional<LocalDateTime> init(LocalDateTime agg, List<LocalDateTime> values) {
        ZoneOffset zone = ZoneOffset.of(ZoneOffset.systemDefault().getId());
        LongSummaryStatistics temp = values.stream().map(v -> v.toEpochSecond(zone))
                .collect(Collectors.summarizingLong(Long::longValue));
        return Optional.of(LocalDateTime.ofEpochSecond(temp.getMax(),0,zone));
    }

}
