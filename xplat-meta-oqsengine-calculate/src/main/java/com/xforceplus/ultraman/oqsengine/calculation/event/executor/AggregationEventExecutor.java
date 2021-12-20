package com.xforceplus.ultraman.oqsengine.calculation.event.executor;

import com.xforceplus.ultraman.oqsengine.calculation.event.dto.CalculationEvent;
import com.xforceplus.ultraman.oqsengine.calculation.event.helper.CalculationEventResource;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CachedEntityClass;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class AggregationEventExecutor implements CalculationEventExecutor {

    @Override
    public boolean execute(CalculationEvent calculationEvent, CachedEntityClass cachedEntityClass, CalculationEventResource resource) {
        return true;
    }
}
