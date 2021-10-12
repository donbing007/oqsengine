package com.xforceplus.ultraman.oqsengine.calculation.factory;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.impl.DefaultCalculationImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 计算实例工厂.
 *
 * @author dongbin
 * @version 0.1 2021/09/17 15:50
 * @since 1.8
 */
public class CalculationFactory {

    private static Map<CalculationScenarios, Calculation> calculations;

    static {
        calculations = new HashMap<>();

        Calculation calculation = new DefaultCalculationImpl();
        calculations.put(CalculationScenarios.BUILD, calculation);
        calculations.put(CalculationScenarios.REPLACE, calculation);
        calculations.put(CalculationScenarios.REPLACE, calculation);
    }


    /**
     * 根据场景获取计算器实例.
     *
     * @param scenarios 计算触发场景.
     * @return 实例.
     */
    public Optional<Calculation> getCalculation(CalculationScenarios scenarios) {
        return Optional.ofNullable(calculations.get(scenarios));
    }

}
