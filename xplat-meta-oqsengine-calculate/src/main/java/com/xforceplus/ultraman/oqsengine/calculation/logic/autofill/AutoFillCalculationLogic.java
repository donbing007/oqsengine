package com.xforceplus.ultraman.oqsengine.calculation.logic.autofill;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class AutoFillCalculationLogic implements CalculationLogic {

    final Logger logger = LoggerFactory.getLogger(AutoFillCalculationLogic.class);

    //    @Override
    //    public Optional<IValue> calculate(CalculationLogicContext context) throws CalculationException {
    //
    //        AutoFill autoFill = (AutoFill) context.getFocusField().config().getCalculation();
    //
    //        switch (autoFill.getDomainNoType()) {
    //            case NORMAL: {
    //                return onNormal(context);
    //            }
    //            case SENIOR: {
    //                return onSenior(context, autoFill);
    //            }
    //            default: {
    //                throw new CalculationException(String.format("autoFill executed failed, unSupport domainNoType-[%s].", autoFill.getDomainNoType().name()));
    //            }
    //        }
    //    }

    @Override
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
        return Optional.empty();
    }

    @Override
    public void scope(CalculationContext context, Infuence infuence) {

    }

    @Override
    public long[] getMaintainTarget(CalculationContext context, Participant participant, Collection<IEntity> entities)
        throws CalculationException {
        return new long[0];
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.AUTO_FILL;
    }

    //    private Optional<IValue> onNormal(CalculationLogicContext context) throws CalculationException {
    //        Object result = context.getBizIDGenerator().nextId(String.valueOf(context.getFocusField().id()));
    //        if (null == result) {
    //            throw new CalculationException("autoFill id generate is null.");
    //        }
    //        return Optional.of(IValueUtils.toIValue(context.getFocusField(), result.toString()));
    //    }
    //
    //    private Optional<IValue> onSenior(CalculationLogicContext context, AutoFill autoFill) throws CalculationException {
    //        try {
    //            //  调用公式执行器执行
    //            return Optional.of(IValueUtils.toIValue(context.getFocusField(),
    //                FormulaHelper.calculate(autoFill.getExpression(), autoFill.getArgs(), context)));
    //        } catch (Exception e) {
    //            logger.warn("autoFill [entityFieldId-{}] has executed failed, execution will broken, [reason-{}]",
    //                context.getFocusField().id(), e.getMessage());
    //            throw new CalculationException(e.getMessage(), e);
    //        }
    //    }
}
