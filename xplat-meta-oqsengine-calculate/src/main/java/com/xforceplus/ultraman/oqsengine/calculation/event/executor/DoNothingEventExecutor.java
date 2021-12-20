package com.xforceplus.ultraman.oqsengine.calculation.event.executor;

import com.xforceplus.ultraman.oqsengine.calculation.event.dto.CalculationEvent;
import com.xforceplus.ultraman.oqsengine.calculation.event.helper.CalculationEventResource;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CachedEntityClass;
import java.sql.SQLException;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class DoNothingEventExecutor implements CalculationEventExecutor {

    /**
     * 这是一个什么都不做的操作.
     */
    @Override
    public boolean execute(CalculationEvent feedBack, CachedEntityClass cachedEntityClass, CalculationEventResource resource) throws SQLException {
        return true;
    }
}
