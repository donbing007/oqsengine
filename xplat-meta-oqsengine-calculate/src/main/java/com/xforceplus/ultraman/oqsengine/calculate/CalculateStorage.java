package com.xforceplus.ultraman.oqsengine.calculate;

import com.googlecode.aviator.Expression;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExpressionWrapper;
import java.util.List;
import java.util.Map;

/**
 * 公式接口.
 *
 * @author j.xu
 * @version 0.1 2021/5/10 14:32
 * @since 1.8
 */
public interface CalculateStorage {

    /**
     * 编译表达式.
     *
     * @param expression 表达式.
     * @return 编译后的Expression对象.
     */
    Expression compile(String expression);

    /**
     * 执行多个表达式，包含层级关系，同时使用一份context.
     *
     * @param executionWrappers target expression lists.
     * @param params            target params.
     * @return object
     */
    Map<String, Object> execute(List<ExecutionWrapper<?>> executionWrappers, Map<String, Object> params);
}
