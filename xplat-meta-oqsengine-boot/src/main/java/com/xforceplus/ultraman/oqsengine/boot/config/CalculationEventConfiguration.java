package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationEventFactory;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SegmentStorage;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import javax.annotation.Resource;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class CalculationEventConfiguration {

    @Resource
    public CalculationEventFactory calculationEventFactory(@Autowired SegmentStorage storage, @Autowired MetaManager metaManager,
                                                           @Autowired KeyValueStorage keyValueStorage, @Autowired SerializeStrategy serializeStrategy) {
        return new CalculationEventFactory(storage, metaManager, keyValueStorage, serializeStrategy);
    }
}
