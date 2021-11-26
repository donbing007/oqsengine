package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

/**
 * 计算字段类型.
 * 每一个计算字段类型都含有一个优先级.
 * 优先级数据越小,表示其被越优先进行计算.
 *
 * @author dongbin
 * @version 0.1 2021/07/07 16:31
 * @since 1.8
 */
public enum CalculationType {

    /*
     * 未知.不参与
     */
    UNKNOWN((byte) 0),
    /*
      普通静态字段.
     */
    STATIC((byte) 1),
    /*
      公式字段.
     */
    FORMULA((byte) 2, (byte) 2, true, true, false),
    /*
      自增字段.
     */
    AUTO_FILL((byte) 3, (byte) 2, true, false, false),
    /*
    lookup 字段.
     */
    LOOKUP((byte) 4, (byte) 1, true, true, false),
    /*
    聚合字段.
    */
    AGGREGATION((byte) 5, (byte) 1, true, false, false);

    private byte symbol;
    /**
     * 优先级.
     */
    private byte priority;
    /**
     * 是否在创建的时候即使没有改变也需要被计算.
     */
    private boolean buildNeedNotChange;
    /**
     * 是否在更新的时候即使没有改变也需要被计算.
     */
    private boolean replaceNeedNotChange;
    /**
     * 是否在删除的时候即使没有改变也需要被计算.
     */
    private boolean deleteNeedNotChange;

    private CalculationType(byte symbol) {
        this(symbol, (byte) 0, false, false, false);
    }

    private CalculationType(byte symbol, byte priority) {
        this(symbol, priority, false, false, false);
    }

    private CalculationType(
        byte symbol,
        byte priority,
        boolean buildNeedNotChange,
        boolean replaceNeedNotChange,
        boolean deleteNeedNotChange) {

        this.symbol = symbol;
        this.priority = priority;
        this.buildNeedNotChange = buildNeedNotChange;
        this.replaceNeedNotChange = replaceNeedNotChange;
        this.deleteNeedNotChange = deleteNeedNotChange;
    }

    public byte getSymbol() {
        return symbol;
    }

    public byte getPriority() {
        return priority;
    }

    public boolean isBuildNeedNotChange() {
        return buildNeedNotChange;
    }

    public boolean isReplaceNeedNotChange() {
        return replaceNeedNotChange;
    }

    public boolean isDeleteNeedNotChange() {
        return deleteNeedNotChange;
    }

    /**
     * 根据字面量获得实例.
     *
     * @param symbol 字面量.
     * @return 实例.
     */
    public static CalculationType getInstance(byte symbol) {
        for (CalculationType type : CalculationType.values()) {
            if (type.getSymbol() == symbol) {
                return type;
            }
        }

        return UNKNOWN;
    }
}
