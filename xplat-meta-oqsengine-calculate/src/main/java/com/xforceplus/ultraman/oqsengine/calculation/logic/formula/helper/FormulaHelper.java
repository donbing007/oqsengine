package com.xforceplus.ultraman.oqsengine.calculation.logic.formula.helper;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.utils.aviator.AviatorHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 08/2021.
 *
 * @since 1.8
 */
public class FormulaHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FormulaHelper.class);
    public static final String FORMULA_CTX_PARAM = "FORMULA_CTX_PARAM";

    private static ExecutionWrapper<?> toExecutionWrapper(String expression, List<String> args, IEntity entity, IEntityClass entityClass)
        throws CalculationException {

        ExpressionWrapper expressionWrapper = ExpressionWrapper.Builder.anExpression()
            .withExpression(expression)
            .withCached(true)
            .build();

        Map<String, Object> runtimeParams = toRuntimeParams(args, entity, entityClass);

        return new ExecutionWrapper<>(expressionWrapper, runtimeParams);
    }

    private static Map<String, Object> toRuntimeParams(List<String> args, IEntity entity, IEntityClass entityClass)
        throws CalculationException {
        Map<String, Object> map = new HashMap<>();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("runtimeArgs is {}", args);
        }
        if (null != args) {
            for (String arg : args) {
                Optional<IValue> valueOp = entity.entityValue().getValue(arg);
                if (valueOp.isPresent()) {
                    map.put(arg, valueOp.get().getValue());
                } else {
                    Optional<IEntityField> entityFieldOptional =
                        entityClass.field(arg);

                    Object defaultValue = null;
                    if (entityFieldOptional.isPresent()) {
                        IEntityField entityField = entityFieldOptional.get();
                        if (entityField.calculationType() == CalculationType.FORMULA) {
                            defaultValue = ((Formula) entityField.config().getCalculation()).getFailedDefaultValue();
                        }
                    }
                    map.put(arg, defaultValue);
                }
            }
        }

        return map;
    }

    /**
     * 使用公式引擎执行公式.
     *
     * @param expression 执行的表达式
     * @param args       参数列表
     * @param context    上下文
     * @return Object the Object
     * @throws CalculationException exception
     */
    public static Object calculate(String expression, List<String> args, CalculationContext context)
        throws CalculationException {
        //  获取公式执行对象
        ExecutionWrapper<?> executionWrapper =
            toExecutionWrapper(expression, args, context.getFocusEntity(), context.getFocusClass());

        executionWrapper.getParams().put(FORMULA_CTX_PARAM, context.getFocusField());

        Object object = AviatorHelper.execute(executionWrapper);
        if (null == object) {
            throw new CalculationException("formula executed, but result is null.");
        }
        return object;
    }
}
