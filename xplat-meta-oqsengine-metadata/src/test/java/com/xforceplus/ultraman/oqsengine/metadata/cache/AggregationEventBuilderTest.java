package com.xforceplus.ultraman.oqsengine.metadata.cache;

import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.test.tools.core.container.basic.RedisContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.List;

/**
 * 聚合事件测试.
 *
 * @author: wangzheng.
 */
@ExtendWith({RedisContainer.class})
public class AggregationEventBuilderTest {

    /**
     * 构建聚合事件.
     *
     */
    @Test
    public void buildAggEvent(String appId, int version,
                              List<EntityClassStorage> storageList, List<Event<?>> payLoads) {
        AggregationEventBuilder aggregationEventBuilder = new AggregationEventBuilder();
    }

    /**
     * 将EntityClassStorage集合转成IEntityClass集合.
     *
     */
    @Test
    public void getAggEntityClass(List<EntityClassStorage> storageList) {

    }

    /**
     * 根据对象id和字段id找到匹配的EntityClass-支持租户.
     *
     */
    @Test
    public void profileByField(long entityClassId, long fieldId, List<EntityClassStorage> storageList) {

    }



}
