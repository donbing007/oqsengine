package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.helper.AggregationAttachmentHelper;
import com.xforceplus.ultraman.oqsengine.calculation.utils.BigDecimalSummaryStatistics;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Optional<IValue> excute(Optional<IValue> agg, ValueChange valueChange) {
        Optional<IValue> o = valueChange.getOldValue();
        Optional<IValue> n = valueChange.getNewValue();
        if (!(agg.isPresent() & o.isPresent() && n.isPresent())) {
            return Optional.empty();
        }

        IValue aggCopyValue = agg.get().copy();
        if (!n.isPresent() || n.get() instanceof EmptyTypedValue) {
            return Optional.of(aggCopyValue);
        }

        boolean invalid;
        // 用以判断数量是否为0.
        final int zeroCount = 0;
        if (!agg.isPresent()) {
            invalid = true;
        } else if (agg.isPresent() && AggregationAttachmentHelper.count(agg.get()) <= zeroCount) {
            invalid = true;
        } else {
            invalid = false;
        }

        if (invalid) {

            // 这里需要重新复制一份的原因是需要增加附件.
            aggCopyValue = IValueUtils.min(aggCopyValue.getField()).copy(
                (String) aggCopyValue.getAttachment().orElse(null));

        }

        IValue newValue = n.get();

        int result = newValue.compareTo(aggCopyValue);
        if (result > 0) {
            aggCopyValue = agg.get().copy(newValue.getValue());
        }

        return Optional.of(aggCopyValue);
    }

    @Override
    public Optional<IValue> init(Optional<IValue> agg, List<Optional<IValue>> values) {
        Optional<IValue> aggValue = Optional.of(agg.get().copy());
        if (agg.get() instanceof DecimalValue) {
            BigDecimalSummaryStatistics temp = values.stream().map(v -> ((DecimalValue) v.get()).getValue())
                .collect(BigDecimalSummaryStatistics.statistics());
            aggValue.get().setStringValue(temp.getMax().toString());
        } else if (agg.get() instanceof LongValue) {
            LongSummaryStatistics temp =
                values.stream().map(o -> o.get()).collect(Collectors.summarizingLong(IValue::valueToLong));
            aggValue.get().setStringValue(String.valueOf(temp.getMax()));
        } else if (agg.get() instanceof DateTimeValue) {
            LongSummaryStatistics temp = values.stream().map(v -> v.get().valueToLong())
                .collect(Collectors.summarizingLong(Long::longValue));
            aggValue.get().setStringValue(String.valueOf(temp.getMax()));
        }
        return Optional.of(aggValue.get());
    }

}
