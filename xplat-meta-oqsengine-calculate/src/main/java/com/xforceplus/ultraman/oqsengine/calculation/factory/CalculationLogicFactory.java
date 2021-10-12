package com.xforceplus.ultraman.oqsengine.calculation.factory;

import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.UnknownCalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.AggregationCalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.autofill.AutoFillCalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.formula.FormulaCalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.LookupCalculationLogic;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import java.util.Collection;
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

    private static Map<CalculationType, CalculationLogic> calculationLogics;

    /**
     * 构造计算逻辑工厂.
     */
    public CalculationLogicFactory() {
        calculationLogics = new HashMap<>();

        CalculationLogic logic = new LookupCalculationLogic();
        calculationLogics.put(logic.supportType(), logic);

        logic = new FormulaCalculationLogic();
        calculationLogics.put(logic.supportType(), logic);

        logic = new AutoFillCalculationLogic();
        calculationLogics.put(logic.supportType(), logic);

        logic = new AggregationCalculationLogic();
        calculationLogics.put(logic.supportType(), logic);
    }

    /**
     * 获取指定计算类型的计算逻辑.
     *
     * @param type 计算类型.
     * @return 计算逻辑.
     */
    public CalculationLogic getCalculationLogic(CalculationType type) {

        CalculationLogic calculationLogic = calculationLogics.get(type);
        if (calculationLogic == null) {
            return UnknownCalculationLogic.getInstance();
        } else {
            return calculationLogic;
        }
    }

    /**
     * 获取所有的计算类型的计算逻辑.
     *
     * @return 计算逻辑实例列表.
     */
    public Collection<CalculationLogic> getCalculationLogics() {
        return calculationLogics.values();
    }
}
