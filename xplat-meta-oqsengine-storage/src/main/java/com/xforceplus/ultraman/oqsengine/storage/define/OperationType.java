package com.xforceplus.ultraman.oqsengine.storage.define;

/**
 * 数据操作类型.
 *
 * @author dongbin
 * @version 0.1 2020/11/4 11:36
 * @since 1.8
 */
public enum OperationType {
    UNKNOWN(0),
    CREATE(1),
    UPDATE(2),
    DELETE(3);

    private int type;

    private OperationType(int type) {
        this.type = type;
    }

    public int getValue() {
        return type;
    }

    /**
     * 获取实例.
     *
     * @param type 字面量.
     * @return 实例.
     */
    public static OperationType getInstance(int type) {
        switch (type) {
            case 1:
                return OperationType.CREATE;
            case 2:
                return OperationType.UPDATE;
            case 3:
                return OperationType.DELETE;
            default:
                return OperationType.UNKNOWN;
        }
    }
}
