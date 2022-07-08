package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.dto.agg.CollectAttachment;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.helper.AggregationAttachmentHelper;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import java.util.List;
import java.util.Optional;

/**
 * Created by justin.xu on 07/2022.
 *
 * @since 1.8
 */
public class CollectFunction implements AggregationFunction {

    //  capacity为2000.
    private static int MAX_CAPACITY = 2000;

    /**
     * collect只处理agg为Strings类型的字段.
     *
     * @param agg           聚合的字段.
     * @param valueChange   值改变的对象.
     * @return 值对象.
     */
    @Override
    public Optional<IValue> excute(Optional<IValue> agg, ValueChange valueChange) {
        //  agg字段类型必须为STRINGS.
        if (!agg.isPresent() || !agg.get().getField().type().equals(FieldType.STRINGS)) {
            return Optional.empty();
        }

        Optional<IValue> o = valueChange.getOldValue();
        Optional<IValue> n = valueChange.getNewValue();

        //  新旧值为null，新旧值同时为EmptyTypedValue，则返回聚合字段原值.
        IValue aggCopy = agg.get().copy();
        if (!o.isPresent() || !n.isPresent() || (o.get() instanceof EmptyTypedValue && n.get() instanceof EmptyTypedValue)) {
            return Optional.of(aggCopy);
        }

        //  修改时，修改前和修改后未发生变化.
        if (o.get().valueToString().equals(n.get().valueToString())) {
            return Optional.of(aggCopy);
        }

        Optional<CollectAttachment> collectAttachmentOp =
            AggregationAttachmentHelper.buildCollectAttachment((String[]) aggCopy.getValue(),
                                                    (String) aggCopy.getAttachment().orElse(""));

        if (!collectAttachmentOp.isPresent()) {
            return Optional.empty();
        }

        CollectAttachment collectAttachment = collectAttachmentOp.get();

        if (o.get() instanceof EmptyTypedValue) {
            //  新增
            collectAttachment.compareAndOperation(n.get().valueToString(), true, MAX_CAPACITY);
        } else if (n.get() instanceof EmptyTypedValue) {
            //  删除
            collectAttachment.compareAndOperation(o.get().valueToString(), false, MAX_CAPACITY);
        } else {
            //  修改, 先删除old
            collectAttachment.compareAndOperation(o.get().valueToString(), false, MAX_CAPACITY);
            //  再新增new
            collectAttachment.compareAndOperation(n.get().valueToString(), true, MAX_CAPACITY);
        }

        return Optional.ofNullable(collectAttachment.toIValue(aggCopy.getField()));
    }

    @Override
    public Optional<IValue> init(Optional<IValue> agg, List<Optional<IValue>> values) {
        //  agg字段类型必须为STRINGS
        if (!agg.isPresent() || !agg.get().getField().type().equals(FieldType.STRINGS)) {
            return Optional.empty();
        }

        IValue aggValue = new StringsValue(agg.get().copy().getField(), new String[0], "");

        Optional<CollectAttachment> collectAttachmentOp =
            AggregationAttachmentHelper.buildCollectAttachment((String[]) aggValue.getValue(),
                (String) aggValue.getAttachment().orElse(""));

        CollectAttachment collectAttachment = collectAttachmentOp.get();

        values.forEach(
            op -> {
                //  新增
                op.ifPresent(
                    value -> collectAttachment.compareAndOperation(value.valueToString(), true, MAX_CAPACITY));
            }
        );

        return Optional.ofNullable(collectAttachment.toIValue(aggValue.getField()));
    }
}
