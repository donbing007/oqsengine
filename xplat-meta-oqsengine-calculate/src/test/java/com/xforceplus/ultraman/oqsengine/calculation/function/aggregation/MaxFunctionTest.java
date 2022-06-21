package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.MaxFunction;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.helper.AggregationAttachmentHelper;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 聚合最大值测试.
 *
 * @className: AvgFunctionTest
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function
 * @author: wangzheng
 * @date: 2021/8/26 19:40
 */
public class MaxFunctionTest {

    private IEntityField aggField = EntityField.Builder.anEntityField()
        .withId(100)
        .withFieldType(FieldType.LONG)
        .withName("agg")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(
                    Aggregation.Builder.anAggregation()
                        .withAggregationType(AggregationType.MAX)
                        .build()
                ).build()
        ).build();

    /**
     * 第一次创建,由于统计数量为0,应该判定给出的第一个值即是目标值.
     */
    @Test
    public void testBuildFirst() throws Exception {
        MaxFunction function = new MaxFunction();

        IValue aggValue = new LongValue(aggField, 0,
            AggregationAttachmentHelper.buildAttachment(0, 0));

        Optional<IValue> newAggValue = function.excute(Optional.of(aggValue),
            ValueChange.build(
                1,
                new EmptyTypedValue(aggField),
                new LongValue(aggField, -20)));

        Assertions.assertEquals(-20L, newAggValue.get().getValue());

        aggValue = new LongValue(aggField, 0,
            AggregationAttachmentHelper.buildAttachment(0, 0));

        newAggValue = function.excute(Optional.of(aggValue),
            ValueChange.build(
                1,
                new EmptyTypedValue(aggField),
                new LongValue(aggField, 20)));
        Assertions.assertEquals(20L, newAggValue.get().getValue());
        Assertions.assertEquals("0|0", newAggValue.get().getAttachment().get());
    }

    @Test
    public void testBuildHaveOld() throws Exception {
        MaxFunction function = new MaxFunction();

        IValue aggValue = new LongValue(aggField, 13,
            AggregationAttachmentHelper.buildAttachment(3, 0));

        Optional<IValue> newAggValue = function.excute(Optional.of(aggValue),
            ValueChange.build(
                1,
                new LongValue(aggField, -10),
                new LongValue(aggField, 12)));
        Assertions.assertEquals(13L, newAggValue.get().getValue());

        aggValue = new LongValue(aggField, 13,
            AggregationAttachmentHelper.buildAttachment(3, 0));

        newAggValue = function.excute(Optional.of(aggValue),
            ValueChange.build(
                1,
                new LongValue(aggField, -10),
                new LongValue(aggField, 17)));
        Assertions.assertEquals(17L, newAggValue.get().getValue());
    }

}
