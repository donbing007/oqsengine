package com.xforceplus.ultraman.oqsengine.calculation.logic.formula;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
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
public class FormulaCalculationLogic implements CalculationLogic {

    final Logger logger = LoggerFactory.getLogger(FormulaCalculationLogic.class);

    private static final int MAX_ERROR_MESSAGE_LENGTH = 256;

//    @Override
//    public Optional<IValue> calculate(CalculationLogicContext context) throws CalculationException {
//
//        Formula formula = (Formula) context.getFocusField().config().getCalculation();
//
//        //  执行公式
//        try {
//            //  调用公式执行器执行
//            return Optional.of(IValueUtils.toIValue(context.getFocusField(),
//                FormulaHelper.calculate(formula.getExpression(), formula.getArgs(), context)));
//        } catch (Exception e) {
//            //  异常时
//            if (formula.getFailedPolicy().equals(Formula.FailedPolicy.USE_FAILED_DEFAULT_VALUE)) {
//                try {
//                    logger.warn(
//                        "formula [entityFieldId-{}] has executed failed, will use failed default value to instead, [reason-{}]",
//                        context.getFocusField().id(), e.getMessage());
//
//                    return Optional.of(IValueUtils.toIValue(context.getFocusField(), formula.getFailedDefaultValue()));
//                } finally {
//                    //  将错误加入hints中
//                    context.hint(e.getMessage().substring(0, Math.min(e.getMessage().length(), MAX_ERROR_MESSAGE_LENGTH)));
//                }
//            } else {
//                logger.warn("formula [entityFieldId-{}] has executed failed, execution will broken, [reason-{}]",
//                    context.getFocusField().id(), e.getMessage());
//                throw new CalculationException(e.getMessage(), e);
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
    public long[] getMaintainTarget(CalculationContext context, IEntityClass entityClass, IEntityField field,
                                    Collection<IEntity> entities) throws CalculationException {
        return new long[0];
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.FORMULA;
    }

}
