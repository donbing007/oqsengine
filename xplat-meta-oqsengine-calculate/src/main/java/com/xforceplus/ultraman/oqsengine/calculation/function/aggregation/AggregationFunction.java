package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

import javax.swing.text.html.Option;
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
    Optional<IValue> excute(Optional<IValue> agg, Optional<IValue> o, Optional<IValue> n);

    /**
     * 合初始化方法.
     *
     * @param agg 聚合字段值.
     * @param values 需要计算的值集合.
     * @return 返回计算值.
     */
    Optional<IValue> init(Optional<IValue> agg, List<Optional<IValue>> values);
}
