package com.xforceplus.ultraman.oqsengine.common.mode;

/**
 * oqs的工作模式定义.
 *
 * @author dongbin
 * @version 0.1 2020/11/20 10:47
 * @since 1.8
 */
public enum OqsMode {
    UNKNOWN(0),
    NORMAL(1),
    READ_ONLY(2);

    private int value;

    OqsMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * 获取实例.
     *
     * @param value 字面值.
     * @return 实例.
     */
    public static OqsMode getInstance(int value) {
        for (OqsMode mode : OqsMode.values()) {
            if (mode.getValue() == value) {
                return mode;
            }
        }

        return UNKNOWN;
    }
}
