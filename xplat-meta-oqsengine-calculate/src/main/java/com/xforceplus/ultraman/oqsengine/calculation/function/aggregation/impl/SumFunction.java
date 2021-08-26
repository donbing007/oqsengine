package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.BigDecimalSummaryStatistics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 求和聚合.
 *
 * @className: SumFunction
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/23 18:32
 */
public class SumFunction implements AggregationFunction {
    @Override
    public Optional<IValue> excute(IValue agg, IValue o, IValue n) {
        if (!(agg != null & o != null && n != null)) {
            return Optional.empty();
        }
        if (o instanceof DecimalValue) {
            BigDecimal temp = ((DecimalValue)agg).getValue()
                    .add(((DecimalValue)n).getValue())
                    .subtract(((DecimalValue)o).getValue());
            agg.setStringValue(temp.toString());
            return Optional.of(agg);
        } else if (o instanceof LongValue) {
            Long temp = agg.valueToLong() + n.valueToLong() - o.valueToLong();
            agg.setStringValue(temp.toString());
            return Optional.of(agg);
        }
        return Optional.empty();
    }

//    public static void main(String[] args) {
//        List<Integer> ints = Arrays.asList(1, 2, 3, 4);
//        System.out.println(ints.stream().collect(Collectors.summarizingInt(Integer::intValue)));
//
//        List<BigDecimal> bigs = Arrays.asList(new BigDecimal("100.1"), new BigDecimal("200.1")
//                , new BigDecimal("300.123223"), new BigDecimal("400.1"));
//        System.out.println(bigs.stream().collect(BigDecimalSummaryStatistics.statistics()));
//        System.out.println(bigs.stream().collect(Collectors.summarizingDouble(BigDecimal::doubleValue)));
//
//        List<IValue> values = Arrays.asList(new DecimalValue(new EntityField(),new BigDecimal("100.0191")), new DecimalValue(new EntityField(),new BigDecimal("100"))
//                , new DecimalValue(new EntityField(),new BigDecimal("100")));
//        System.out.println(values.stream().map(v -> {
//            return (DecimalValue)v;
//        }).collect(Collectors.toList()).stream().collect(Collectors.summarizingDouble(DecimalValue::valueToLong)));
//    }

}
