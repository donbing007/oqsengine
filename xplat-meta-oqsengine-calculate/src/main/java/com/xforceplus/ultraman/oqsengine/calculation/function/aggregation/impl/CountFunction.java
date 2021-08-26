package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.List;
import java.util.Optional;

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
        return Optional.empty();
    }

}
