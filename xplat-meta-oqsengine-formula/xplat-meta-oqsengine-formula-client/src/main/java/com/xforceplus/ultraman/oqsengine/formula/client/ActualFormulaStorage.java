package com.xforceplus.ultraman.oqsengine.formula.client;

import com.googlecode.aviator.Expression;
import com.xforceplus.ultraman.oqsengine.formula.client.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.formula.client.exception.FormulaClientException;
import com.xforceplus.ultraman.oqsengine.formula.client.utils.ExpressionUtils;
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
    public boolean compile(ExpressionWrapper expression) {
        return ExpressionUtils.compile(expression) != null;
    }

    @Override
    public Object execute(ExpressionWrapper expressionWrapper, Map<String, Object> params) {
        Expression expression = ExpressionUtils.compile(expressionWrapper);
        if (null == expression) {
            throw new FormulaClientException(String.format("compile expression failed [%s-%s].",
                                        expressionWrapper.getCode(), expressionWrapper.getExpression()));
        }
        return expression.execute(params);
    }
}
