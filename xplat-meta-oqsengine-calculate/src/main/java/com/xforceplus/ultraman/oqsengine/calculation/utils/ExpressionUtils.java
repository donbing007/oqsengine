package com.xforceplus.ultraman.oqsengine.calculation.utils;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import java.math.MathContext;

/**
 * 表达式工具类.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/10
 * @since 1.8
 */
public class ExpressionUtils {
    //  LRU-CACHE最大缓存公式个数
    private static final int CAPACITY = 1024 * 10;
    //  函数内默认最大循环次数
    private static final int MAX_LOOP_COUNT = 8;
    private static final AviatorEvaluatorInstance INSTANCE;

    static {
        INSTANCE = AviatorEvaluator.getInstance().useLRUExpressionCache(CAPACITY);
        //  允许高精度计算模式
        INSTANCE.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
        //  使用DECIMAL128作为精度标准，即precision为34
        INSTANCE.setOption(Options.MATH_CONTEXT, MathContext.DECIMAL128);
        //  最大循环次数
        INSTANCE.setOption(Options.MAX_LOOP_COUNT, MAX_LOOP_COUNT);
    }

    /**
     * 编译一个函数.
     */
    public static Expression compile(ExpressionWrapper expressionWrapper) {
        String functionBody = AviatorHelper.parseRule(expressionWrapper.getExpression());
        return INSTANCE.compile(expressionWrapper.getCode(), functionBody, expressionWrapper.isCached());
    }

    /**
     * 编译并执行一个函数.
     */
    public static Object execute(ExecutionWrapper executionWrapper) throws CalculationLogicException {
        Expression expression = compile(executionWrapper.getExpressionWrapper());
        if (null == expression) {
            throw new CalculationLogicException(String.format("compile [expression-%s] failed .",
                executionWrapper.getExpressionWrapper().getExpression()));
        }
        return expression.execute(executionWrapper.getParams());
    }

    /**
     * 注册一个自定义函数.
     *
     * @param function 待添加的函数实例
     */
    public static void addFunction(AviatorFunction function) {
        INSTANCE.addFunction(function);
    }
}
