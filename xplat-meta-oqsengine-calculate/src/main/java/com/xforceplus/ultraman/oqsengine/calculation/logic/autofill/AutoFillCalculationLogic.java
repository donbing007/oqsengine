package com.xforceplus.ultraman.oqsengine.calculation.logic.autofill;

import com.xforceplus.ultraman.oqsengine.calculation.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.calculation.utils.CalculationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
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

    @Override
    public Optional<IValue> calculate(CalculationLogicContext context) throws CalculationLogicException {

        AutoFill autoFill = (AutoFill) context.getFocusField().config().getCalculation();

        switch (autoFill.getDomainNoType()) {
            case NORMAL: {
                return onNormal(context);
            }
            case SENIOR: {
                return onSenior(context, autoFill);
            }
            default: {
                throw new CalculationLogicException(String.format("autoFill executed failed, unSupport domainNoType-[%s].", autoFill.getDomainNoType().name()));
            }
        }
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.AUTO_FILL;
    }

    private Optional<IValue> onNormal(CalculationLogicContext context) throws CalculationLogicException {
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

    private Optional<IValue> onSenior(CalculationLogicContext context, AutoFill autoFill) throws CalculationLogicException {
        try {
            return CalculationHelper.calculate(autoFill.getExpression(), autoFill.getArgs(), context);
        } catch (Exception e) {
            logger.warn("autoFill [entityFieldId-{}] has executed failed, execution will broken, [reason-{}]",
                context.getFocusField().id(), e.getMessage());
            throw e;
        }
    }
}
