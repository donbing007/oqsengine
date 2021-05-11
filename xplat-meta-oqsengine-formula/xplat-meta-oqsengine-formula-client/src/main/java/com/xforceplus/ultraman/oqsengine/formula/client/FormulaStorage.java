package com.xforceplus.ultraman.oqsengine.formula.client;

import com.xforceplus.ultraman.oqsengine.formula.client.dto.ExpressionWrapper;
import java.util.Map;

/**
 * 公式对外接口.
 *
 * @author j.xu
 * @version 0.1 2021/5/10 14:32
 * @since 1.8
 */
public interface FormulaStorage {

    /**
     * compile expression.
     *
     * @param expression target expression.
     * @return boolean
     */
    boolean compile(ExpressionWrapper expression);


    /**
     * execute expression, if expression not compile, function will compile this expression.
     *
     * @param expression target expression.
     * @param params target params.
     * @return object
     */
    Object execute(ExpressionWrapper expression, Map<String, Object> params);
}
