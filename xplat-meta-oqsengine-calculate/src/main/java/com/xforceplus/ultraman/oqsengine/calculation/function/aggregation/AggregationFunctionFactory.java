package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;

/**
 * 聚合函数工厂类.
 *
 * @className: AggregationFunctionFactory
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/31 16:54
 */
public interface AggregationFunctionFactory {

    /**
     * 获取聚合函数实现.
     *
     * @param aggregationType 函数类型.
     * @return 函数实现.
     */

    AggregationFunction getAggregationFunction(AggregationType aggregationType);

}
