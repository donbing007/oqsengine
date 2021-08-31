package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.*;
import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;

/**
 * 聚合计算函数.
 *
 * @className: AggregationFunctionFactory
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/31 16:47
 */
public class AggregationFunctionFactoryImpl implements AggregationFunctionFactory{

    @Override
    public AggregationFunction getAggregationFunction(AggregationType aggregationType) {
        switch (aggregationType) {
            case COUNT:
                return new CountFunction();
            case AVG:
                return new AvgFunction();
            case MAX:
                return new MaxFunction();
            case MIN:
                return new MinFunction();
            case SUM:
                return new SumFunction();
            default:
                throw new IllegalArgumentException(
                        String.format("Unrecognized physical aggregationFunction type.[%d]", aggregationType.toString()));
        }
    }
}
