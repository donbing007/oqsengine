package com.xforceplus.ultraman.oqsengine.calculation.logic;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Collection;
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
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
        return Optional.empty();
    }

    @Override
    public void scope(CalculationContext context, Infuence infuence) {

    }

    @Override
    public long[] getMaintainTarget(CalculationContext context, IEntityClass entityClass, IEntityField field,
                                    Collection<IEntity> entities) throws CalculationException {
        return new long[0];
    }

    @Override
    public CalculationScenarios[] needMaintenanceScenarios() {
        return new CalculationScenarios[0];
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.UNKNOWN;
    }
}
