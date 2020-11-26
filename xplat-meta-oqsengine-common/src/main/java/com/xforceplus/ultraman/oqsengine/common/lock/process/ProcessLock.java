package com.xforceplus.ultraman.oqsengine.common.lock.process;

import com.xforceplus.ultraman.oqsengine.common.lock.ILock;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.common.lock.LockHelper.DEFAULT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.common.lock.LockHelper.RETRY_DELAY;

/**
 * desc :
 * name : ProcessLock
 *
 * @author : xujia
 * date : 2020/9/7
 * @since : 1.8
 */
public class ProcessLock implements ILock {

    private String resourceId;
    private String value;
    private long timeoutInMillis;
    private DefaultProcessLockHandler defaultProcessLockHandler;

    public ProcessLock(String resourceId, long timeout, TimeUnit timeUnit,
                       DefaultProcessLockHandler defaultProcessLockHandler) {

        this.resourceId = resourceId;
        this.value = UUID.randomUUID().toString();
        this.defaultProcessLockHandler = defaultProcessLockHandler;
        this.timeoutInMillis = (0 < timeout && null != timeUnit) ?
                TimeUnit.MILLISECONDS.convert(timeout, timeUnit) : DEFAULT_TIME_OUT;
    }

    @Override
    public boolean isFreeLock() throws SQLException {
        String v = defaultProcessLockHandler.isLocked(resourceId);

        if (null == v) {
            return true;
        }
        return false;
    }

    @Override
    public boolean tryLock() {
        boolean ok = false;
        long timePass = 0;
        long delay = getRetryDelay();
        while (!ok) {
            ok = defaultProcessLockHandler.lock(resourceId, value);

            if (!ok) {
                await(delay);
                timePass += delay;
                if (timePass >= timeoutInMillis) {
                    break;
                }
            }
        }

        return ok;
    }

    @Override
    public boolean releaseLock() {
        String v = defaultProcessLockHandler.isLocked(resourceId);

        if (null == v) {
            return true;
        }

        if (v.equals(value)) {
            return defaultProcessLockHandler.remove(resourceId);
        }
        //  not the lock master
        return false;
    }

    @Override
    public boolean forceUnLock(String key) {
        return defaultProcessLockHandler.remove(resourceId);
    }

    private long getRetryDelay() {
        return RETRY_DELAY;
    }

    private void await(long time) {
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
