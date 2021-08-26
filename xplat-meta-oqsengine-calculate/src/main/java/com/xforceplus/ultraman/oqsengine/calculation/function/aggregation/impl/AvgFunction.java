package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.BigDecimalSummaryStatistics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.ZoneOffset;
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
    public Optional<IValue> excute(IValue agg, IValue o, IValue n) {
        if (!(agg != null & o != null && n != null)) {
            return Optional.empty();
        }
        if (agg instanceof DecimalValue) {
            BigDecimal temp = ((DecimalValue)agg).getValue()
                    .add(((DecimalValue)n).getValue())
                    .subtract(((DecimalValue)o).getValue());
            agg.setStringValue(temp.toString());
            return Optional.of(agg);
        } else if (agg instanceof LongValue) {
            Long temp = agg.valueToLong() + n.valueToLong() - o.valueToLong();
            agg.setStringValue(temp.toString());
            return Optional.of(agg);
        }
        return Optional.empty();
    }

    @Override
    public Optional<IValue> init(IValue agg, List<IValue> values) {
        if (agg instanceof DecimalValue) {
            BigDecimalSummaryStatistics temp = values.stream().map(v -> ((DecimalValue)v).getValue())
                    .collect(BigDecimalSummaryStatistics.statistics());
            agg.setStringValue(temp.getAverage(MathContext.DECIMAL128).toString());
        } else if (agg instanceof LongValue) {
            LongSummaryStatistics temp = values.stream().collect(Collectors.summarizingLong(IValue::valueToLong));
            agg.setStringValue(String.valueOf(temp.getAverage()));
        }
        return Optional.of(agg);
    }

    public Optional<IValue> excute(IValue agg, IValue o, IValue n, int count) {
        Optional<IValue> sum = this.excute(agg, o, n);
        if (sum.get() != null) {
            if (o instanceof DecimalValue) {
                BigDecimal temp = ((DecimalValue)sum.get()).getValue().divide(new BigDecimal(count));
                agg.setStringValue(temp.toString());
                return Optional.of(agg);
            } else if (o instanceof LongValue) {
                Long temp = sum.get().valueToLong() / count;
                agg.setStringValue(temp.toString());
                return Optional.of(agg);
            }
        }
        return Optional.empty();
    }


}
