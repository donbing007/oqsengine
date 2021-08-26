package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.List;
import java.util.Optional;

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
    public Optional<IValue> excute(List<IValue> values) {
        return Optional.empty();
    }

}
