package com.xforceplus.ultraman.oqsengine.calculation.event.executor;

import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationEvent;
import com.xforceplus.ultraman.oqsengine.calculation.event.helper.CalculationEventResource;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CachedEntityClass;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.CalculationInitStatus;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import org.checkerframework.checker.units.qual.C;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class AggregationEventExecutor extends AbstractEventExecutor {

    @Override
    public boolean execute(CalculationEvent calculationEvent, CachedEntityClass cachedEntityClass, CalculationEventResource resource) {
        return kvProcess(calculationEvent, resource);
    }
}
