package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
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
    public Optional<IValue> excute(IValue agg, IValue o, IValue n) {
        if (n == null) {
            return Optional.of(agg);
        }
        if (agg instanceof LongValue) {
            if (o == null) {
                Long temp = agg.valueToLong() + 1;
                agg.setStringValue(temp.toString());
            }
        }
        return Optional.of(agg);
    }

    @Override
    public Optional<IValue> init(IValue agg, List<IValue> values) {
        if (agg instanceof LongValue) {
            LongSummaryStatistics temp = values.stream().collect(Collectors.summarizingLong(IValue::valueToLong));
            agg.setStringValue(String.valueOf(temp.getCount()));
        }
        return Optional.of(agg);
    }

}
