package com.xforceplus.ultraman.oqsengine.formula.utils;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.xforceplus.ultraman.oqsengine.formula.dto.ExpressionWrapper;

/**
 * 表达式工具类.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/10
 * @since 1.8
 */
public class ExpressionUtils {

    public static Expression compile(ExpressionWrapper expressionWrapper) {
        return AviatorEvaluator.getInstance()
            .compile(expressionWrapper.getCode(),  expressionWrapper.getExpression(), expressionWrapper.isCached());
    }
}
