package com.xforceplus.ultraman.oqsengine.lock.utils;

import java.util.UUID;

/**
 * 锁定者信息.
 *
 * @author dongbin
 * @version 0.1 2022/1/4 11:08
 * @since 1.8
 */
public class Locker {

    // 加锁者名称.
    private String name;
    // 当前剩余加锁数量.
    private int successLockNumber;

    public Locker() {
        name = UUID.randomUUID().toString();
        successLockNumber = 0;
    }

    public void incrSuccess() {
        this.incrSuccess(1);
    }

    public void incrSuccess(int size) {
        successLockNumber += size;
    }

    public void decrSuccess() {
        successLockNumber -= 1;
    }

    /**
     * size.
     */
    public void decrSuccess(int size) {
        successLockNumber -= size;
        if (successLockNumber < 0) {
            successLockNumber = 0;
        }
    }

    public String getName() {
        return name;
    }

    public int getSuccessLockNumber() {
        return successLockNumber;
    }
}
