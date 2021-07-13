package com.xforceplus.ultraman.oqsengine.calculation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Optional;

/**
 * 处理未知计算.
 *
 * @author dongbin
 * @version 0.1 2021/07/07 16:42
 * @since 1.8
 */
public class UnknownCalculationLogic implements CalculationLogic {

    private static final CalculationLogic INSTANCE = new UnknownCalculationLogic();

    public static CalculationLogic getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<IValue> calculate(CalculationLogicContext context) throws CalculationLogicException {
        return Optional.empty();
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.UNKNOWN;
    }
}
