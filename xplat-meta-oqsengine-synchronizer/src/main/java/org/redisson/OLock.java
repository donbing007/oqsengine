package org.redisson;


import org.redisson.api.RLockAsync;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * copy from Redisson
 */
public interface OLock extends OLockAsync {

    /**
     * Returns name of object
     *
     * @return name - name of object
     */
    String getName();

    /**
     * Tries to acquire the lock with defined <code>leaseTime</code>.
     * Waits up to defined <code>waitTime</code> if necessary until the lock became available.
     * <p>
     * Lock will be released automatically after defined <code>leaseTime</code> interval.
     *
     * @param waitTime  the maximum time to acquire the lock
     * @param leaseTime lease time
     * @param unit      time unit
     * @return <code>true</code> if lock is successfully acquired,
     * otherwise <code>false</code> if lock is already set.
     * @throws InterruptedException - if the thread is interrupted
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit, String threadId) throws InterruptedException;


    void unlock(String theadId);

    /**
     * Unlocks the lock independently of its state
     *
     * @return <code>true</code> if lock existed and now unlocked
     * otherwise <code>false</code>
     */
    boolean forceUnlock();

    /**
     * Checks if the lock locked by any thread
     *
     * @return <code>true</code> if locked otherwise <code>false</code>
     */
    boolean isLocked();

    /**
     * Checks if the lock is held by thread with defined <code>threadId</code>
     *
     * @param threadId Thread ID of locking thread
     * @return <code>true</code> if held by thread with given id
     * otherwise <code>false</code>
     */
    boolean isHeldByThread(String threadId);

    /**
     * Number of holds on this lock by the current thread
     *
     * @return holds or <code>0</code> if this lock is not held by current thread
     */
    int getHoldCount(String threadId);

    /**
     * Remaining time to live of the lock
     *
     * @return time in milliseconds
     * -2 if the lock does not exist.
     * -1 if the lock exists but has no associated expire.
     */
    long remainTimeToLive();

}

