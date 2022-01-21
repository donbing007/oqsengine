package com.xforceplus.ultraman.oqsengine.core.service.impl.calculator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 聚合测试功能.
 *
 * @className: EntityManagementWithAggregation
 * @package: com.xforceplus.ultraman.oqsengine.core.service.impl.calculator
 * @author: wangzheng
 * @date: 2021/9/14 10:00
 */
public class EntityManagementWithAggregationTest {

    @Test
    public void findAggregationsTest() {
        List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> entities = findAggregationAndReplace(null, 10);
        System.out.println(entities.size());
    }

    private List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> findAggregationAndReplace(List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> entities, int count) {
        if (entities == null) {
            entities = new ArrayList<>();
        }
        if (count > 0) {
            entities.add(Entity.Builder.anEntity().withId(count).build());
            count = count - 1;
            findAggregationAndReplace(entities, count);
        }
        return entities;
    }

}
