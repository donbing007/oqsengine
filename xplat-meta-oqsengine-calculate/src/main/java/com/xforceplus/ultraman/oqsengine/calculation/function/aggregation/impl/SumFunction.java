package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.BigDecimalSummaryStatistics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;

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
    public Optional<IValue> excute(List<IValue> values) {
        if (values.size() < 2) {
            return Optional.empty();
        }
        //取第一个value的字段类型作为转换
        if (values.get(0) instanceof DecimalValue) {
            BigDecimalSummaryStatistics bds = values.stream().map(v -> ((DecimalValue)v).getValue()).collect(Collectors.toList())
                    .stream().collect(BigDecimalSummaryStatistics.statistics());
        } else if (values.get(0) instanceof LongValue) {
            LongSummaryStatistics lss = values.stream().collect(Collectors.summarizingLong(IValue::valueToLong));
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
