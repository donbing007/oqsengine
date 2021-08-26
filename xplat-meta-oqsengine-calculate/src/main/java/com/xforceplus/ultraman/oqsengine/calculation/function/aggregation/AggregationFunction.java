package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.List;
import java.util.Optional;

/**
 * 聚合字段方法.
 *
 * @className: AggregationFunction
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/23 18:50
 */
public interface AggregationFunction {

    /**
     * 聚合执行.
     *
     */
    public Optional<IValue> excute(IValue agg, IValue o, IValue n);
}
