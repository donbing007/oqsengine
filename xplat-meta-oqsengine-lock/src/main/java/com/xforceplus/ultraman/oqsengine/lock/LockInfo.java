package com.xforceplus.ultraman.oqsengine.lock;

import java.io.Serializable;

/**
 * 锁信息.
 *
 * @author dongbin
 * @version 0.1 2021/08/09 17:37
 * @since 1.8
 */
public class LockInfo implements Serializable {

    private final String lockingId;
    private int number;

    public LockInfo(String lockingId) {
        this.lockingId = lockingId;
        number = 1;
    }

    public String getLockingId() {
        return lockingId;
    }

    public int incr() {
        return number++;
    }

    public int dec() {
        return --number;
    }

    public int getNumber() {
        return number;
    }

}
