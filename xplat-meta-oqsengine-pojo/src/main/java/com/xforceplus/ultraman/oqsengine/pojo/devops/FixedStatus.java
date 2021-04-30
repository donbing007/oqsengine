package com.xforceplus.ultraman.oqsengine.pojo.devops;

/**
 * desc :
 * name : FixedStatus
 *
 * @author : xujia
 * date : 2020/11/21
 * @since : 1.8
 */
public enum  FixedStatus {
    NOT_FIXED(1),
    SUBMIT_FIX_REQ(2),
    FIXED(3),
    FIX_ERROR(4);

    private int status;

    public int getStatus() {
        return status;
    }

    FixedStatus(int status) {
        this.status = status;
    }
}
