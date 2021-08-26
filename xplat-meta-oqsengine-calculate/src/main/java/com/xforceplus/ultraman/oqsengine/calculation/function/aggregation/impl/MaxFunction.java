package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.BigDecimalSummaryStatistics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;

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
    public Optional<IValue> excute(IValue agg, IValue o, IValue n) {
        if (!(agg != null & o != null && n != null)) {
            return Optional.empty();
        }
        if (agg instanceof DecimalValue) {
            double temp = Math.max(Math.max(((DecimalValue)n).getValue().doubleValue(),
                    ((DecimalValue)o).getValue().doubleValue())
                    , ((DecimalValue)agg).getValue().doubleValue());
            agg.setStringValue(String.valueOf(temp));
            return Optional.of(agg);
        } else if (agg instanceof LongValue) {
            long temp = Math.max(Math.max(n.valueToLong(),o.valueToLong()),agg.valueToLong());
            agg.setStringValue(String.valueOf(temp));
            return Optional.of(agg);
        } else if (agg instanceof DateTimeValue) {
            ZoneOffset zone = ZoneOffset.of(ZoneOffset.systemDefault().getId());
            ((DateTimeValue)n).getValue().toEpochSecond(zone);
            long temp = Math.max(Math.max(((DateTimeValue)n).getValue().toEpochSecond(zone),
                    ((DateTimeValue)o).getValue().toEpochSecond(zone)),
                    ((DateTimeValue)agg).getValue().toEpochSecond(zone));
            agg.setStringValue(String.valueOf(temp));
            return Optional.of(agg);
        }
        return Optional.empty();
    }

    @Override
    public Optional<IValue> init(IValue agg, List<IValue> values) {
        if (agg instanceof DecimalValue) {
            BigDecimalSummaryStatistics temp = values.stream().map(v -> ((DecimalValue)v).getValue())
                    .collect(BigDecimalSummaryStatistics.statistics());
            agg.setStringValue(temp.getMax().toString());
        } else if (agg instanceof LongValue) {
            LongSummaryStatistics temp = values.stream().collect(Collectors.summarizingLong(IValue::valueToLong));
            agg.setStringValue(String.valueOf(temp.getMax()));
        } else if (agg instanceof DateTimeValue) {
            ZoneOffset zone = ZoneOffset.of(ZoneOffset.systemDefault().getId());
            LongSummaryStatistics temp = values.stream().map(v -> ((DateTimeValue)v).getValue().toEpochSecond(zone))
                    .collect(Collectors.summarizingLong(Long::longValue));
            agg.setStringValue(String.valueOf(temp.getMax()));
        }
        return Optional.of(agg);
    }

}
