package com.xforceplus.ultraman.oqsengine.calculate;

import com.googlecode.aviator.Expression;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.utils.ExpressionUtils;
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
public class ActualCalculateStorage implements CalculateStorage {

    @Override
    public Expression compile(String expression) {
        return ExpressionUtils.compile(ExpressionWrapper.Builder.anExpression()
            .withExpression(expression).withCached(true).build());
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
                    //  将执行结果写入context中
                    result.put(executionWrapper.getCode(),
                        ExpressionUtils.execute(executionWrapper.getExpressionWrapper(), result));
                }
            }
        }

        return result;
    }


}