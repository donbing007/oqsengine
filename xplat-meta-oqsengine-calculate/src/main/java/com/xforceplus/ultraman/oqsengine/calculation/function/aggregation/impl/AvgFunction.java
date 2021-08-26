package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * 平均值聚合.
 *
 * @className: AvgFunction
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function.aggregation
 * @author: wangzheng
 * @date: 2021/8/23 18:33
 */
public class AvgFunction implements AggregationFunction {

    @Override
    public Optional<IValue> excute(IValue agg, IValue o, IValue n) {
        if (!(agg != null & o != null && n != null)) {
            return Optional.empty();
        }
        if (o instanceof DecimalValue) {
            double temp = Math.max(Math.max(((DecimalValue)n).getValue().doubleValue(),
                    ((DecimalValue)o).getValue().doubleValue())
                    , ((DecimalValue)agg).getValue().doubleValue());
            agg.setStringValue(String.valueOf(temp));
            return Optional.of(agg);
        } else if (o instanceof LongValue) {
            long temp = Math.max(Math.max(n.valueToLong(),o.valueToLong()),agg.valueToLong());
            agg.setStringValue(String.valueOf(temp));
            return Optional.of(agg);
        } else if (o instanceof DateTimeValue) {
            ZoneOffset zone = ZoneOffset.of(ZoneOffset.systemDefault().getId());
            ((DateTimeValue)n).getValue().toEpochSecond(zone);
            long temp = Math.max(Math.max(((DateTimeValue)n).getValue().toEpochSecond(zone),
                    ((DateTimeValue)o).getValue().toEpochSecond(zone)),
                    ((DateTimeValue)agg).getValue().toEpochSecond(zone));
            agg.setStringValue(String.valueOf(temp));
        }
        return Optional.empty();
    }

}
