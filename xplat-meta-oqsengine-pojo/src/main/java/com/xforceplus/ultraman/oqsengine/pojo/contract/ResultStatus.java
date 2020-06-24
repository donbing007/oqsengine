package com.xforceplus.ultraman.oqsengine.pojo.contract;

/**
 * 响应状态.
 * @author dongbin
 * @version 0.1 2020/6/23 14:25
 * @since 1.8
 */
public enum ResultStatus {

    UNKNOWN(0),
    SUCCESS(1),
    CONFLICT(2);

    private int value;

    private ResultStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
