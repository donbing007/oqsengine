package com.xforceplus.ultraman.oqsengine.metadata.utils.storage;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.EntityClassSyncProtoBufMocker;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.ExpectedEntityStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import java.util.List;
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
    public void protoToStorageListTest() {
        String appId = "testLoad";
        String appCode = "protoToStorageListTest";
        int expectedVersion = 1;
        long expectedId = 1 + 7200;

        List<ExpectedEntityStorage> expectedEntityStorageList =
            EntityClassSyncProtoBufMocker.mockSelfFatherAncestorsGenerate(expectedId);

        EntityClassSyncResponse entityClassSyncResponse =
            EntityClassSyncProtoBufMocker.Response
                .entityClassSyncResponseGenerator(appId, appCode, expectedVersion, expectedEntityStorageList);

        List<EntityClassStorage> result =
            EntityClassStorageBuilderUtils.protoToStorageList(entityClassSyncResponse.getEntityClassSyncRspProto());

        Assertions.assertEquals(expectedEntityStorageList.size(), result.size());

        expectedEntityStorageList.forEach(
            expectedEntityStorage -> {
                EntityClassStorage entityClassStorage =
                    result.stream().filter(r -> r.getId() == expectedEntityStorage.getSelf()).findFirst().orElse(null);

                Assertions.assertNotNull(entityClassStorage);

                Assertions.assertEquals(expectedEntityStorage.getFather(), entityClassStorage.getFatherId());
            }
        );
    }

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
    public void convertConditions() {

    }

}
