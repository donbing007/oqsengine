package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * 最小值聚合.
 *
 * @className: MinFunction
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/23 18:33
 */
public class MinFunction implements AggregationFunction {

    @Override
    public Optional<IValue> excute(IValue agg, IValue o, IValue n) {
        if (!(agg != null & o != null && n != null)) {
            return Optional.empty();
        }
        if (o instanceof DecimalValue) {
            double temp = Math.min(Math.min(((DecimalValue)n).getValue().doubleValue(),
                    ((DecimalValue)o).getValue().doubleValue())
                    , ((DecimalValue)agg).getValue().doubleValue());
            agg.setStringValue(String.valueOf(temp));
            return Optional.of(agg);
        } else if (o instanceof LongValue) {
            long temp = Math.min(Math.min(n.valueToLong(),o.valueToLong()),agg.valueToLong());
            agg.setStringValue(String.valueOf(temp));
            return Optional.of(agg);
        } else if (o instanceof DateTimeValue) {
            ZoneOffset zone = ZoneOffset.of(ZoneOffset.systemDefault().getId());
            ((DateTimeValue)n).getValue().toEpochSecond(zone);
            long temp = Math.min(Math.min(((DateTimeValue)n).getValue().toEpochSecond(zone),
                    ((DateTimeValue)o).getValue().toEpochSecond(zone)),
                    ((DateTimeValue)agg).getValue().toEpochSecond(zone));
            agg.setStringValue(String.valueOf(temp));
        }
        return Optional.empty();
    }

}
