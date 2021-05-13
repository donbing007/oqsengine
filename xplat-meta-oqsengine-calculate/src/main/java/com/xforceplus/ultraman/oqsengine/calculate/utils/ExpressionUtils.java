package com.xforceplus.ultraman.oqsengine.calculate.utils;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExpressionWrapper;

/**
 * 表达式工具类.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/10
 * @since 1.8
 */
public class ExpressionUtils {

    private static final int CAPACITY = 1024 * 10;
    private static final AviatorEvaluatorInstance INSTANCE;

    static {
        INSTANCE = AviatorEvaluator.getInstance().useLRUExpressionCache(CAPACITY);
    }

    public static Expression compile(ExpressionWrapper expressionWrapper) {
        return INSTANCE.compile(expressionWrapper.getCode(),
                            expressionWrapper.getExpression(), expressionWrapper.isCached());
    }
}
