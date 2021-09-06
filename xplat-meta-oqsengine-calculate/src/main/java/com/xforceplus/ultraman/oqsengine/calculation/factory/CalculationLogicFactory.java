package com.xforceplus.ultraman.oqsengine.calculation.factory;

import com.xforceplus.ultraman.oqsengine.calculation.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import java.util.Collection;

/**
 * 计算逻辑工厂接口.
 *
 * @author dongbin
 * @version 0.1 2021/08/24 14:56
 * @since 1.8
 */
public interface CalculationLogicFactory {

    /**
     * 获取指定计算字段的计算逻辑.
     *
     * @param type 计算字段类型.
     * @return 计算逻辑.
     */
    public CalculationLogic getCalculation(CalculationType type);

    /**
     * 获取所有的计算逻辑.
     *
     * @return 计算逻辑列表.
     */
    public Collection<CalculationLogic> getCalculations();
}
