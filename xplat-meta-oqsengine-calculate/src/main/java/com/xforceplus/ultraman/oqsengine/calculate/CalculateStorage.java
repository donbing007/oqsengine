package com.xforceplus.ultraman.oqsengine.calculate;

import com.googlecode.aviator.Expression;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * @param executionWrappers expression wrappers.
     * @param params            context.
     * @return key(left) is the final valueList, value(right) is the keys who calculate failed to use defaultValue.
     */
    AbstractMap.SimpleEntry<List<IValue>, Map<String, String>>
        execute(List<ExecutionWrapper<?>> executionWrappers, Map<String, Object> params);
}
