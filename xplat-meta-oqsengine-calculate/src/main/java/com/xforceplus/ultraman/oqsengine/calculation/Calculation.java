package com.xforceplus.ultraman.oqsengine.calculation;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

/**
 * 实例的计算字段计算.
 *
 * @author dongbin
 * @version 0.1 2021/09/17 15:12
 * @since 1.8
 */
public interface Calculation {

    /**
     * 针对计算字段进行计算.
     *
     * @param context 计算的上下文.
     * @return 计算后的结果.
     * @throws CalculationException 计算异常.
     */
    public IEntity calculate(CalculationContext context) throws CalculationException;

    /**
     * 影响的实体维护.
     *
     * @param context 维护上下文.
     * @throws CalculationException 维护错误.
     */
    public void maintain(CalculationContext context) throws CalculationException;

}
