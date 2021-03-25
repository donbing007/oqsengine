package com.xforceplus.ultraman.oqsengine.event;

/**
 * 事件类型.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 10:34
 * @since 1.8
 */
public enum EventType {
    /**
     * 未知.
     */
    UNKNOWN(0),
    /**
     * 事务开始.
     */
    TX_BEGIN(1),
    /**
     * 事务已经提交.
     */
    TX_COMMITED(2),
    /**
     * 事务已经回滚.
     */
    TX_ROLLBACKED(3),
    /**
     * 事务准备提交.
     */
    TX_PREPAREDNESS_COMMIT(4),
    /**
     * 事务准备回滚.
     */
    TX_PREPAREDNESS_ROLLBACK(5),
    /**
     * 实例创建.
     */
    ENTITY_BUILD(6),
    /**
     * 实例替换.
     */
    ENTITY_REPLACE(7),
    /**
     * 实例删除.
     */
    ENTITY_DELETE(8);

    private int value;

    private EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EventType getInstance(int value) {
        for (EventType t : EventType.values()) {
            if (t.getValue() == value) {
                return t;
            }
        }

        return EventType.UNKNOWN;
    }
}
