package com.xforceplus.ultraman.oqsengine.event;

/**
 * 事件的优先级.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 10:52
 * @since 1.8
 */
public enum EventPriority {
    /**
     * 未知.
     */
    UNKNOWN(0),
    /**
     * 低优先级.
     */
    LOW(1),
    /**
     * 正常优先级.
     */
    NORMAL(2),
    /**
     * 高优先级.
     */
    HIGH(3);

    private int value;

    private EventPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EventPriority getInstance(int value) {
        for (EventPriority t : EventPriority.values()) {
            if (t.getValue() == value) {
                return t;
            }
        }

        return EventPriority.UNKNOWN;
    }
}
