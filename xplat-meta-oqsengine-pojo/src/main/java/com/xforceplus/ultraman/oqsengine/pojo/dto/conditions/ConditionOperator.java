package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

/**
 * 查询条件的可用操作符.
 * @author dongbin
 * @version 0.1 2020/2/20 13:26
 * @since 1.8
 */
public enum ConditionOperator {
    LIKE("LIKE"),
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUALS(">="),
    MINOR_THAN("<"),
    MINOR_THAN_EQUALS("<=");

    private String symbol;

    private ConditionOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static ConditionOperator getInstance(String symbol) {
        String noSpaceSymbol = symbol.trim();
        for (ConditionOperator operator : ConditionOperator.values()) {
            if (operator.getSymbol().equals(noSpaceSymbol)) {
                return operator;
            }
        }

        return null;
    }
}
