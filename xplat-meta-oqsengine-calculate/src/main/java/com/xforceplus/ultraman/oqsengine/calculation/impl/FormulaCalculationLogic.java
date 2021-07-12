package com.xforceplus.ultraman.oqsengine.calculation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ExpressionUtils;
import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationHint;
import com.xforceplus.ultraman.oqsengine.calculation.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import java.util.HashMap;
import java.util.Map;
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

    @Override
    public Optional<IValue> calculate(CalculationLogicContext context) throws CalculationLogicException {
        //  执行公式
        Formula formula = (Formula) context.getFocusField().config().getCalculationDefinition();

        try {
            //  获取公式执行对象
            ExecutionWrapper<?> executionWrapper = toExecutionWrapper(formula, context.getEntity());

            Object object = ExpressionUtils.execute(executionWrapper);
            if (null == object) {
                throw new CalculationLogicException("formula executed, but result is null.");
            }

            return Optional.of(IValueUtils.toIValue(context.getFocusField(), object));
        } catch (Exception e) {
            //  异常时
            if (formula.getFailedPolicy().equals(Formula.FailedPolicy.USE_FAILED_DEFAULT_VALUE)) {
                try {
                    logger.warn(
                        "formula [entityFieldId-{}] has executed failed, will use failed default value to instead, [reason-{}]",
                        context.getFocusField().id(), e.getMessage());

                    return Optional.of(IValueUtils.toIValue(context.getFocusField(), formula.getFailedDefaultValue()));
                } finally {
                    //  将错误加入hints中
                    context.getHints().add(new CalculationHint(context.getFocusField(),
                        e.getMessage().substring(0, Math.min(e.getMessage().length(), MAX_ERROR_MESSAGE_LENGTH))));
                }
            } else {
                logger.warn("formula [entityFieldId-{}] has executed failed, execution will broken, [reason-{}]",
                    context.getFocusField().id(), e.getMessage());
                throw e;
            }
        }
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.FORMULA;
    }


    private ExecutionWrapper<?> toExecutionWrapper(Formula formula, IEntity entity) throws CalculationLogicException {

        ExpressionWrapper expressionWrapper = ExpressionWrapper.Builder.anExpression()
            .withExpression(formula.getExpression())
            .withCached(true)
            .build();

        Map<String, Object> runtimeParams = toRuntimeParams(formula, entity);

        return new ExecutionWrapper<>(expressionWrapper, runtimeParams);

    }

    private Map<String, Object> toRuntimeParams(Formula formula, IEntity entity) throws CalculationLogicException {
        Map<String, Object> map = new HashMap<>();
        for (String arg : formula.getArgs()) {
            Optional<IValue> iValueOp = entity.entityValue().getValue(arg);

            if (iValueOp.isPresent()) {
                map.put(arg, iValueOp.get().getValue());
            } else {
                throw new CalculationLogicException(String.format("formula execution absence param [%s]", arg));
            }
        }

        return map;
    }
}
