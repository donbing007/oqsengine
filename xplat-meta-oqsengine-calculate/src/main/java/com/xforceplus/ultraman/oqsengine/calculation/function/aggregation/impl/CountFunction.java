package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.BigDecimalSummaryStatistics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    public Optional<IValue> excute(Optional<IValue> agg, Optional<IValue> o, Optional<IValue> n) {
        if (!n.isPresent()) {
            return Optional.of(agg.get());
        }
        if (agg.get() instanceof LongValue) {
            if (!o.isPresent()) {
                Long temp = agg.get().valueToLong() + 1;
                agg.get().setStringValue(temp.toString());
            }
        }
        return Optional.of(agg.get());
    }

    @Override
    public Optional<IValue> init(Optional<IValue> agg, List<Optional<IValue>> values) {
        if (agg.get() instanceof LongValue) {
            LongSummaryStatistics temp = values.stream().map(o -> o.get()).collect(Collectors.summarizingLong(IValue::valueToLong));
            agg.get().setStringValue(String.valueOf(temp.getCount()));
        }
        return Optional.of(agg.get());
    }

    @Override
    public Optional<Long> init(long agg, List<Long> values) {
        LongSummaryStatistics temp = values.stream().collect(Collectors.summarizingLong(Long::longValue));
        return Optional.of(temp.getCount());
    }

    @Override
    public Optional<BigDecimal> init(BigDecimal agg, List<BigDecimal> values) {
        BigDecimalSummaryStatistics temp = values.stream().collect(BigDecimalSummaryStatistics.statistics());
        return Optional.of(BigDecimal.valueOf(temp.getCount()));
    }

    @Override
    public Optional<LocalDateTime> init(LocalDateTime agg, List<LocalDateTime> values) {
        return Optional.empty();
    }

}
