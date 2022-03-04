package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.calculation.event.CalculateEventDispatcher;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationEventFactory;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SegmentStorage;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
@Configuration
public class CalculationEventConfiguration {

    @Bean
    public CalculationEventFactory calculationEventFactory(@Autowired SegmentStorage storage,
                                                           @Autowired MetaManager metaManager,
                                                           @Autowired KeyValueStorage keyValueStorage,
                                                           @Autowired SerializeStrategy serializeStrategy) {
        return new CalculationEventFactory(storage, metaManager, keyValueStorage, serializeStrategy);
    }

    /**
     * calculateEventDispatcher bean定义.
     *
     * @return eventDispatcher.
     */
    @DependsOn({"calculationEventFactory", "eventBus"})
    @Bean
    public CalculateEventDispatcher calculateEventDispatcher() {
        return new CalculateEventDispatcher();
    }
}
