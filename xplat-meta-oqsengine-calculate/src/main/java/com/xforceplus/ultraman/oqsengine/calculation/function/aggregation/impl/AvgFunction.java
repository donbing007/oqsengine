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
    public Optional<IValue> excute(IValue agg, IValue o, IValue n) {
        if (!(agg != null & o != null && n != null)) {
            return Optional.empty();
        }
        if (agg instanceof DecimalValue) {
            BigDecimal temp = ((DecimalValue) agg).getValue()
                    .add(((DecimalValue) n).getValue())
                    .subtract(((DecimalValue) o).getValue());
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
            BigDecimalSummaryStatistics temp = values.stream().map(v -> ((DecimalValue) v).getValue())
                    .collect(BigDecimalSummaryStatistics.statistics());
            agg.setStringValue(temp.getAverage(MathContext.DECIMAL64).toString());
        } else if (agg instanceof LongValue) {
            LongSummaryStatistics temp = values.stream().collect(Collectors.summarizingLong(IValue::valueToLong));
            agg.setStringValue(new DecimalFormat("0").format(temp.getAverage()));
        }
        return Optional.of(agg);
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
    public Optional<IValue> excuteAvg(IValue agg, IValue o, IValue n, int count) {
        if (agg instanceof DecimalValue) {
            BigDecimal temp = ((DecimalValue) agg).getValue()
                    .multiply(new BigDecimal(count), MathContext.DECIMAL64)
                    .add(((DecimalValue) n).getValue())
                    .subtract(((DecimalValue) o).getValue())
                    .divide(new BigDecimal(count), MathContext.DECIMAL64);
            agg.setStringValue(temp.toString());
            return Optional.of(agg);
        } else if (agg instanceof LongValue) {
            Long temp = (agg.valueToLong() * count + n.valueToLong() - o.valueToLong()) / count;
            agg.setStringValue(temp.toString());
            return Optional.of(agg);
        }
        return Optional.empty();
    }

}
