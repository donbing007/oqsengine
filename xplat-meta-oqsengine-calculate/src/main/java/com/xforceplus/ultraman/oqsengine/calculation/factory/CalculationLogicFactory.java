package com.xforceplus.ultraman.oqsengine.calculation.factory;

import com.xforceplus.ultraman.oqsengine.calculation.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.autofill.AutoFillCalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.formula.FormulaCalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.LookupCalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.UnknownCalculationLogic;
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
        calculations.put(CalculationType.FORMULA, new FormulaCalculationLogic());
        calculations.put(CalculationType.AUTO_FILL, new AutoFillCalculationLogic());
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
