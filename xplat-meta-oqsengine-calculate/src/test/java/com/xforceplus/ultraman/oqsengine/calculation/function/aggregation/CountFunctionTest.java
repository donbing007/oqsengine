package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.CountFunction;
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
 * 总数测试.
 *
 * @className: CountFunctionTest
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function
 * @author: wangzheng
 * @date: 2021/8/26 19:40
 */
public class CountFunctionTest {

    private IEntityField aggField = EntityField.Builder.anEntityField()
        .withId(100)
        .withFieldType(FieldType.LONG)
        .withName("agg")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(
                    Aggregation.Builder.anAggregation()
                        .withAggregationType(AggregationType.COUNT)
                        .build()
                ).build()
        ).build();

    /**
     * 测试第一次创建.
     */
    @Test
    public void testBuildFirst() throws Exception {
        CountFunction function = new CountFunction();

        IValue aggValue = new LongValue(aggField, 0, "0|0");
        Optional<IValue> newAggValue = function.excute(Optional.of(aggValue),
            ValueChange.build(
                1,
                new EmptyTypedValue(aggField),
                new LongValue(aggField, 1)));

        Assertions.assertEquals(1L, newAggValue.get().getValue());
        Assertions.assertEquals("0|0", newAggValue.get().getAttachment().get());
    }

    /**
     * 测试已有旧值.
     */
    @Test
    public void testBuildHaveOld() throws Exception {
        CountFunction function = new CountFunction();
        IValue aggValue = new LongValue(aggField, 10);
        Optional<IValue> newAggValue = function.excute(Optional.of(aggValue),
            ValueChange.build(
                1,
                new EmptyTypedValue(aggField),
                new LongValue(aggField, 1)));
        Assertions.assertEquals(11L, newAggValue.get().getValue());
    }

    /**
     * 删除已经有的值.
     */
    @Test
    public void testDeleteNotOld() throws Exception {
        CountFunction function = new CountFunction();
        IValue aggValue = new LongValue(aggField, 10);
        Optional<IValue> newAggValue = function.excute(Optional.of(aggValue),
            ValueChange.build(
                1,
                new LongValue(aggField, 1),
                new EmptyTypedValue(aggField)));
        Assertions.assertEquals(9L, newAggValue.get().getValue());
    }

}
