package com.xforceplus.ultraman.oqsengine.calculation.factory;

import com.xforceplus.ultraman.oqsengine.calculation.event.executor.AggregationEventExecutor;
import com.xforceplus.ultraman.oqsengine.calculation.event.executor.AutoFillEventExecutor;
import com.xforceplus.ultraman.oqsengine.calculation.event.executor.CalculationEventExecutor;
import com.xforceplus.ultraman.oqsengine.calculation.event.executor.DoNothingEventExecutor;
import com.xforceplus.ultraman.oqsengine.calculation.event.executor.FormulaEventExecutor;
import com.xforceplus.ultraman.oqsengine.calculation.event.executor.LookupEventExecutor;
import com.xforceplus.ultraman.oqsengine.calculation.event.executor.StaticEventExecutor;
import com.xforceplus.ultraman.oqsengine.calculation.event.helper.CalculationEventResource;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SegmentStorage;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class CalculationEventFactory {
    /**
     * 执行器的集合.
     */
    private Map<CalculationType, CalculationEventExecutor> executors;

    /**
     * bean初始化时需要初始化的资源.
     */
    private CalculationEventResource resource;

    /**
     * 构造事件处理工厂.
     */
    public CalculationEventFactory(SegmentStorage storage, MetaManager metaManager, KeyValueStorage keyValueStorage,
                                   SerializeStrategy serializeStrategy) {
        //  注入的初始化类.
        resource = CalculationEventResource.Builder.anCalculationEventContext()
            .withMeta(metaManager)
            .withSegmentStorage(storage)
            .withKeyValueStorage(keyValueStorage)
            .withSerializeStrategy(serializeStrategy)
            .build();

        executors = new HashMap<>();

        executors.put(CalculationType.AUTO_FILL, new AutoFillEventExecutor());

        executors.put(CalculationType.UNKNOWN, new DoNothingEventExecutor());

        executors.put(CalculationType.FORMULA, new FormulaEventExecutor());

        executors.put(CalculationType.LOOKUP, new LookupEventExecutor());

        executors.put(CalculationType.STATIC, new StaticEventExecutor());

        executors.put(CalculationType.AGGREGATION, new AggregationEventExecutor());
    }

    /**
     * 获取当前计算类型的执行器.
     *
     * @param calculationType 计算类型.
     * @return 执行器.
     */
    public CalculationEventExecutor executor(CalculationType calculationType) {
        CalculationEventExecutor executor = executors.get(calculationType);
        return null != executor ? executor : executors.get(CalculationType.UNKNOWN);
    }

    /**
     * 获取当前的计算资源.
     *
     * @return 当前计算资源.
     */
    public CalculationEventResource resource() {
        return resource;
    }
}
