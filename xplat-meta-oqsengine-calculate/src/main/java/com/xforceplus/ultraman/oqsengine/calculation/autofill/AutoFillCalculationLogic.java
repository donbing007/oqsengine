package com.xforceplus.ultraman.oqsengine.calculation.autofill;

import com.xforceplus.ultraman.oqsengine.calculation.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import java.util.Optional;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class AutoFillCalculationLogic implements CalculationLogic {

    @Override
    public Optional<IValue> calculate(CalculationLogicContext context) throws CalculationLogicException {
        //TODO: 还没定义namespace.
        if (!context.getLongContinuousPartialOrderIdGenerator().supportNameSpace()) {
            throw new CalculationLogicException("An invalid ID generator must support namespaces.");
        }
        Object result = context.getLongContinuousPartialOrderIdGenerator().next("");
        if (null == result) {
            throw new CalculationLogicException("autoFill id generate is null.");
        }
        return Optional.of(IValueUtils.toIValue(context.getFocusField(), result));
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.AUTO_FILL;
    }
}
