package com.xforceplus.ultraman.oqsengine.formula;

import com.xforceplus.ultraman.oqsengine.formula.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.formula.dto.ExpressionWrapper;
import java.util.List;
import java.util.Map;

/**
 * 公式接口.
 *
 * @author j.xu
 * @version 0.1 2021/5/10 14:32
 * @since 1.8
 */
public interface FormulaStorage {

    /**
     * 通过expression编译表达式.
     *
     * @param expression target expression.
     * @return boolean
     */
    boolean compile(String expression);

    /**
     * 通过expressionWrapper编译表达式.
     *
     * @param expressionWrapper target expression.
     * @return boolean
     */
    boolean compile(ExpressionWrapper expressionWrapper);

    /**
     * 执行一个表达式，并返回结果.
     *
     * @param expression target expression.
     * @param params target params.
     * @return object
     */
    Object execute(ExpressionWrapper expression, Map<String, Object> params);

    /**
     * 执行多个表达式，包含层级关系，同时使用一份context.
     *
     * @param executionWrappers target expression lists.
     * @param params target params.
     * @return object
     */
    Map<String, Object> execute(List<ExecutionWrapper<?>> executionWrappers, Map<String, Object> params);
}
