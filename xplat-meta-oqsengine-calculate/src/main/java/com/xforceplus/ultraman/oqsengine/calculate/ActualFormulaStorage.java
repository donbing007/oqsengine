package com.xforceplus.ultraman.oqsengine.calculate;

import com.googlecode.aviator.Expression;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.exception.CalculateExecutionException;
import com.xforceplus.ultraman.oqsengine.calculate.utils.ExpressionUtils;
import com.xforceplus.ultraman.oqsengine.calculate.utils.TypeCheck;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公式对外服务.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/10
 * @since 1.8
 */
public class ActualFormulaStorage implements FormulaStorage {

    @Override
    public boolean compile(String expression) {
        return ExpressionUtils.compile(ExpressionWrapper.Builder.anExpression()
            .withExpression(expression).build()) != null;
    }

    @Override
    public boolean compile(ExpressionWrapper expressionWrapper) {
        return ExpressionUtils.compile(expressionWrapper) != null;
    }

    @Override
    public Object execute(ExpressionWrapper expressionWrapper, Map<String, Object> params) {
        Expression expression = ExpressionUtils.compile(expressionWrapper);
        if (null == expression) {
            throw new CalculateExecutionException(String.format("compile expression failed [%s-%s].",
                expressionWrapper.getCode(), expressionWrapper.getExpression()));
        }
        return expression.execute(params);
    }

    @Override
    public Map<String, Object> execute(List<ExecutionWrapper<?>> expressionWrappers, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>(params);

        Map<Integer, List<ExecutionWrapper<?>>> partitionExpressionWraps = new HashMap<>();

        int maxLevel = 0;
        /*
            将所有expressionWrappers按照Level分层
         */
        for (ExecutionWrapper<?> expressionWrapper : expressionWrappers) {
            if (expressionWrapper.getLevel() > maxLevel) {
                maxLevel = expressionWrapper.getLevel();
            }
            partitionExpressionWraps.computeIfAbsent(expressionWrapper.getLevel(), ArrayList::new)
                .add(expressionWrapper);
        }

        /*
            从1-N的顺序执行每一层中的所有表达式
         */
        for (int i = 1; i <= maxLevel; i++) {
            List<ExecutionWrapper<?>> needExecutions = partitionExpressionWraps.get(i);
            if (null != needExecutions) {
                for (ExecutionWrapper<?> executionWrapper : needExecutions) {
                    Object object = execute(executionWrapper.getExpressionWrapper(), params);

                    if (null != object) {
                        /*
                            校验返回类型相符
                        */
                        if (!TypeCheck.check(executionWrapper.getRetClazz(), object)) {
                            throw new CalculateExecutionException(
                                String.format("code-[%s], retType not equals to define, define [%s], actual [%s]",
                                    executionWrapper.getCode(), executionWrapper.getRetClazz().getCanonicalName(),
                                    object.getClass().getCanonicalName()));
                        }

                        result.put(executionWrapper.getCode(), object);
                    }
                }
            }
        }

        return result;
    }


}
