package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

/**
 * 计算字段初始化状态.
 *
 * @version 0.1 2021/11/22 14:05
 * @Auther weikai
 * @since 1.8
 */
public enum CalculationInitStatus {
    UNKNOWN((byte) 0),

    UN_INIT((byte) 1),

    INIT_DONE((byte) 2);

    private byte symbol;


    CalculationInitStatus(byte symbol) {
        this.symbol = symbol;
    }
}
