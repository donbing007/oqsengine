package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.BigDecimalSummaryStatistics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
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
    public Optional<IValue> excute(Optional<IValue> agg, Optional<IValue> o, Optional<IValue> n) {
        if (!(agg != null & o != null && n != null)) {
            return Optional.empty();
        }
        if (agg.get() instanceof DecimalValue) {
            BigDecimal temp = ((DecimalValue) agg.get()).getValue()
                    .add(((DecimalValue) n.get()).getValue())
                    .subtract(((DecimalValue) o.get()).getValue());
            agg.get().setStringValue(temp.toString());
            return Optional.of(agg.get());
        } else if (agg.get() instanceof LongValue) {
            Long temp = agg.get().valueToLong() + n.get().valueToLong() - o.get().valueToLong();
            agg.get().setStringValue(temp.toString());
            return Optional.of(agg.get());
        }
        return Optional.empty();
    }

    @Override
    public Optional<IValue> init(Optional<IValue> agg, List<Optional<IValue>> values) {
        if (agg.get() instanceof DecimalValue) {
            BigDecimalSummaryStatistics temp = values.stream().map(v -> ((DecimalValue) v.get()).getValue())
                    .collect(BigDecimalSummaryStatistics.statistics());
            agg.get().setStringValue(temp.getAverage(MathContext.DECIMAL64).toString());
        } else if (agg.get() instanceof LongValue) {
            LongSummaryStatistics temp = values.stream().map(o -> o.get()).collect(Collectors.summarizingLong(IValue::valueToLong));
            agg.get().setStringValue(new DecimalFormat("0").format(temp.getAverage()));
        }
        return Optional.of(agg.get());
    }

    /**
     * 求平均值.
     *
     * @param agg 聚合值.
     * @param o 老值.
     * @param n 新值.
     * @param count 分子值-总数查询count出的结果.
     * @return 返回计算值.
     */
    public Optional<IValue> excuteAvg(Optional<IValue> agg, Optional<IValue> o, Optional<IValue> n, int count) {
        if (agg.get() instanceof DecimalValue) {
            BigDecimal temp = ((DecimalValue) agg.get()).getValue()
                    .multiply(new BigDecimal(count), MathContext.DECIMAL64)
                    .add(((DecimalValue) n.get()).getValue())
                    .subtract(((DecimalValue) o.get()).getValue())
                    .divide(new BigDecimal(count), MathContext.DECIMAL64);
            agg.get().setStringValue(temp.toString());
            return Optional.of(agg.get());
        } else if (agg.get() instanceof LongValue) {
            Long temp = (agg.get().valueToLong() * count + n.get().valueToLong() - o.get().valueToLong()) / count;
            agg.get().setStringValue(temp.toString());
            return Optional.of(agg.get());
        }
        return Optional.empty();
    }
}
