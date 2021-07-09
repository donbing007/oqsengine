package com.xforceplus.ultraman.oqsengine.calculation;

import com.xforceplus.ultraman.oqsengine.calculation.impl.LookupCalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.impl.UnknownCalculationLogic;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import java.util.HashMap;
import java.util.Map;

/**
 * 计算逻辑的简单工厂.
 *
 * @author dongbin
 * @version 0.1 2021/07/07 16:26
 * @since 1.8
 */
public class CalculationLogicFactory {

    private Map<CalculationType, CalculationLogic> calculations;

    /**
     * 构造计算逻辑工厂.
     */
    public CalculationLogicFactory() {
        calculations = new HashMap<>();

        calculations.put(CalculationType.LOOKUP, new LookupCalculationLogic());

    }

    /**
     * 获取指定计算类型的计算逻辑.
     *
     * @param type 计算类型.
     * @return 计算逻辑.
     */
    public CalculationLogic getCalculation(CalculationType type) {

        CalculationLogic calculationLogic = calculations.get(type);
        if (calculationLogic == null) {
            return UnknownCalculationLogic.getInstance();
        } else {
            return calculationLogic;
        }
    }
}
