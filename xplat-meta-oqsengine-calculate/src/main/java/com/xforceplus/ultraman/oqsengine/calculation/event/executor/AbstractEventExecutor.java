package com.xforceplus.ultraman.oqsengine.calculation.event.executor;

import com.xforceplus.ultraman.oqsengine.calculation.event.helper.CalculationEventResource;
import java.util.List;
import java.util.Map;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public abstract class AbstractEventExecutor implements CalculationEventExecutor {

    public void deleteTypeExecute(Map<Long, List<Long>> deletes, CalculationEventResource resource) {
        //  todo delete
    }
}
