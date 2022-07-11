package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.AvgFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.CollectFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.CountFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.MaxFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.MinFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.SumFunction;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;

/**
 * 聚合计算函数.
 *
 * @className: AggregationFunctionFactory
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/31 16:47
 */
public class AggregationFunctionFactoryImpl {


    /**
     * 聚合函数工厂.
     *
     * @param aggregationType 聚合字段类型.
     * @return 聚合函数执行者.
     */
    public static AggregationFunction getAggregationFunction(AggregationType aggregationType) {
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
            case COLLECT:
                return new CollectFunction();
            default:
                throw new IllegalArgumentException(
                    String.format("Unrecognized physical aggregationFunction type.[%d]", aggregationType.toString()));
        }
    }
}
