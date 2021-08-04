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
    UNKNOWN((byte) 0, (byte) 0),
    /*
      普通静态字段.
     */
    STATIC((byte) 1, (byte) 0),
    /*
      公式字段.
     */
    FORMULA((byte) 2, (byte) 2),
    /*
      自增字段.
     */
    AUTO_FILL((byte) 3, (byte) 2),
    /*
    lookup 字段.
     */
    LOOKUP((byte) 4, (byte) 1);

    private byte symbol;
    private byte priority;

    private CalculationType(byte symbol, byte priority) {
        this.symbol = symbol;
        this.priority = priority;
    }

    public byte getSymbol() {
        return symbol;
    }

    public byte getPriority() {
        return priority;
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
