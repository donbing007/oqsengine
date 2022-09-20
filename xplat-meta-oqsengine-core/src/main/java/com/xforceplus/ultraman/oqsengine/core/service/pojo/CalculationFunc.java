package com.xforceplus.ultraman.oqsengine.core.service.pojo;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;

/**
 * 计算字段功能函数及上下文.
 */
public class CalculationFunc {
    private Calculation calculation;

    private CalculationContext calculationContext;

    public CalculationFunc(Calculation calculation, CalculationContext calculationContext) {
        this.calculation = calculation;
        this.calculationContext = calculationContext;
    }

    public Calculation getCalculation() {
        return calculation;
    }

    public void setCalculation(Calculation calculation) {
        this.calculation = calculation;
    }

    public CalculationContext getCalculationContext() {
        return calculationContext;
    }

    public void setCalculationContext(CalculationContext calculationContext) {
        this.calculationContext = calculationContext;
    }
}
