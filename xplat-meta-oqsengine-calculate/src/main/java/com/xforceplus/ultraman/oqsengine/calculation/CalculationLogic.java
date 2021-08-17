package com.xforceplus.ultraman.oqsengine.calculation;

import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Optional;

/**
 * 字段值计算定义.
 *
 * @author dongbin
 * @version 0.1 2021/07/01 17:40
 * @since 1.8
 */
public interface CalculationLogic {

    /**
     * 字段值计算.
     *
     * @param context 计算上下文.
     * @return 计算结果.
     * @throws CalculationLogicException 计算发生异常.
     */
    public Optional<IValue> calculate(CalculationLogicContext context) throws CalculationLogicException;

    /**
     * 计算字段在写事务最后需要判断是否需要维护外界关系一致性等.
     *
     * @param context 计算上下文.
     * @throws CalculationLogicException 计算发生异常.
     */
    public default void maintain(CalculationLogicContext context) throws CalculationLogicException {
        // do nothing
    }

    /**
     * 支持的计算类型.
     *
     * @return 计算类型.
     */
    public CalculationType supportType();
}
