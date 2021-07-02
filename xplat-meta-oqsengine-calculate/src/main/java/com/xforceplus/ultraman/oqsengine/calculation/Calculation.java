package com.xforceplus.ultraman.oqsengine.calculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Optional;

/**
 * 字段值计算定义.
 *
 * @author dongbin
 * @version 0.1 2021/07/01 17:40
 * @since 1.8
 */
public interface Calculation {

    /**
     * 字段值计算.
     *
     * @param context 计算上下文.
     * @return 计算结果.
     * @throws CalculationException 计算发生异常.
     */
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException;
}
