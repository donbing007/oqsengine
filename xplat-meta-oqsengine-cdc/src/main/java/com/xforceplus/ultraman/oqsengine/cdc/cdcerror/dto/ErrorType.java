package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto;

/**
 * 错误类型.
 *
 * @author xujia 2021/04/22
 * @since : 1.8
 */
public enum ErrorType {
    DATA_FORMAT_ERROR(1),
    DATA_INSERT_ERROR(2);

    private int type;

    public int getType() {
        return type;
    }

    ErrorType(int type) {
        this.type = type;
    }
}
