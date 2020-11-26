package com.xforceplus.ultraman.oqsengine.devops.rebuild.enums;

/**
 * desc :
 * name : BatchStatus
 *
 * @author : xujia
 * date : 2020/8/19
 * @since : 1.8
 */
public enum BatchStatus {
    PENDING(0), RUNNING(1), DONE(2), ERROR(3), CANCEL(4);

    private int code;
    BatchStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static BatchStatus toBatchStatus(int code) {
        for(BatchStatus batchStatus : BatchStatus.values()) {
            if (batchStatus.code == code) {
                return batchStatus;
            }
        }
        return null;
    }
}
