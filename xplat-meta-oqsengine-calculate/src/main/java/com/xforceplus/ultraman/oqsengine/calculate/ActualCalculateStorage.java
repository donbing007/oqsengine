package com.xforceplus.ultraman.oqsengine.calculate;

import com.googlecode.aviator.Expression;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.utils.ExpressionUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import java.util.AbstractMap;
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

    private static final int MIN_LEVEL = 1;
    private static final int MAX_ERROR_MESSAGE_LENGTH = 256;

    @Override
    public Expression compile(String expression) {
        return ExpressionUtils.compile(ExpressionWrapper.Builder.anExpression()
            .withExpression(expression).withCached(true).build());
    }

    @Override
    public AbstractMap.SimpleEntry<List<IValue>, Map<String, String>>
        execute(List<ExecutionWrapper<?>> expressionWrappers, Map<String, Object> params) {
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

        List<IValue> finalValues = new ArrayList<>();
        Map<String, String> failedMaps = new HashMap<>();

        /*
            从1-N的顺序执行每一层中的所有表达式
         */
        for (int i = MIN_LEVEL; i <= maxLevel; i++) {
            List<ExecutionWrapper<?>> needExecutions = partitionExpressionWraps.get(i);
            if (null != needExecutions) {
                for (ExecutionWrapper<?> executionWrapper : needExecutions) {
                    try {
                        //  已存在于计算错误中，则不再进行计算二直接进入出错处理
                        if (i > MIN_LEVEL
                            &&
                            !checkArgs(executionWrapper.getEntityField().calculator().getArgs(), result, failedMaps)) {
                            throw new IllegalArgumentException(
                                String.format("formula [%s-%s] at level [%d] has unexpected arg-result.",
                                    executionWrapper.getCode(), executionWrapper.getExpressionWrapper().getExpression(),
                                    i));
                        }
                        //  执行公式
                        Object object =
                            ExpressionUtils.execute(executionWrapper.getExpressionWrapper(), result);
                        //  公式计算结果不能为空
                        if (null == object) {
                            throw new IllegalStateException(String.format("formula [%s-%s] result could not be null",
                                executionWrapper.getCode(), executionWrapper.getExpressionWrapper().getExpression()));
                        }
                        finalValues.add(IValueUtils.toIValue(executionWrapper.getEntityField(), object));
                        result.put(executionWrapper.getCode(), object);
                    } catch (Exception e) {
                        if (executionWrapper.getEntityField().calculator().getFailedPolicy().equals(
                            Calculator.FailedPolicy.USE_FAILED_DEFAULT_VALUE)) {

                            finalValues.add(IValueUtils.toIValue(executionWrapper.getEntityField(),
                                executionWrapper.getEntityField().calculator().getFailedDefaultValue()));
                        } else {
                            throw e;
                        }
                        //  将错误写入failedMaps中
                        failedMaps.put(executionWrapper.getEntityField().name(),
                            e.getMessage().substring(0, Math.min(e.getMessage().length(), MAX_ERROR_MESSAGE_LENGTH)));
                    }
                }
            }
        }
        return new AbstractMap.SimpleEntry(finalValues, failedMaps);
    }

    private boolean checkArgs(List<String> args, Map<String, Object> result, Map<String, String> failedMaps) {
        if (null != args) {
            for (String arg : args) {
                //  存在于失败列表或者不存在于结果集中,返回失败
                if (failedMaps.containsKey(arg) || !result.containsKey(arg)) {
                    return false;
                }
            }
        }
        return true;
    }

}
