package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

/**
 * 查询条件的可用操作符.
 *
 * @author dongbin
 * @version 0.1 2020/2/20 13:26
 * @since 1.8
 */
public enum ConditionOperator {

    /**
     * 模糊匹配.
     */
    LIKE("LIKE"),

    /**
     * 等于.
     */
    EQUALS("="),

    /**
     * 不等于.
     */
    NOT_EQUALS("!="),

    /**
     * 大于.
     */
    GREATER_THAN(">"),

    /**
     * 大于等于.
     */
    GREATER_THAN_EQUALS(">="),

    /**
     * 小于.
     */
    LESS_THAN("<"),

    /**
     * 小于等于.
     */
    LESS_THAN_EQUALS("<="),

    /**
     * 等于多个值.
     */
    MULTIPLE_EQUALS("IN");

    private String symbol;

    private ConditionOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * 根据字面量获得实例.
     *
     * @param symbol 字面量.
     * @return 实例.
     */
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
