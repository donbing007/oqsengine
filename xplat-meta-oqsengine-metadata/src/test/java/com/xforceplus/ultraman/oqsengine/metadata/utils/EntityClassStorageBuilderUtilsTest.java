package com.xforceplus.ultraman.oqsengine.metadata.utils;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 转换测试.
 *
 * @className: EntityClassStorageBuilderUtilsTest
 * @package: com.xforceplus.ultraman.oqsengine.metadata.utils
 * @author: wangzheng
 * @date: 2021/9/8 15:59
 */
public class EntityClassStorageBuilderUtilsTest {

    @Test
    public void toAggregation() {
        Calculator calculator = Calculator.newBuilder()
            .setAggregationBoId(1000)
            .setAggregationFieldId(10001)
            .setAggregationRelationId(1000001)
            .setAggregationType(1)
            .build();

        Conditions conditions = null;
        Aggregation.Builder builder = Aggregation.Builder.anAggregation()
            .withClassId(calculator.getAggregationBoId())
            .withFieldId(calculator.getAggregationFieldId())
            .withAggregationType(AggregationType.getInstance(calculator.getAggregationType()))
            .withRelationId(calculator.getAggregationRelationId())
            .withConditions(conditions);

        Aggregation aggregation = builder.build();

        Assertions.assertNotNull(aggregation);
        Assertions.assertEquals(aggregation.getClassId(), calculator.getAggregationBoId());

    }

    @Test
    public void converConditions() {

    }

}
