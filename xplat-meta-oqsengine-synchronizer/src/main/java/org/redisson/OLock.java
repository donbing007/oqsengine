package org.redisson;


import java.util.concurrent.TimeUnit;

/**
 * copy from Redisson.
 */
public interface OLock extends OLockAsync {

    /**
     * Returns name of object.
     *
     * @return name - name of object
     */
    String getName();

    /**
     * try lock.
     *
     * @param waitTime  the maximum time to acquire the lock
     * @param leaseTime lease time
     * @param unit      time unit
     * @return <code>true</code> if lock is successfully acquired,
     *     otherwise <code>false</code> if lock is already set.
     * @throws InterruptedException - if the thread is interrupted
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit, String threadId) throws InterruptedException;

    /**
     * unlock.
     *
     * @param theadId threadId is uuid
     */
    void unlock(String theadId);

    /**
     * Unlocks the lock independently of its state.
     *
     * @return <code>true</code> if lock existed and now unlocked
     *     otherwise <code>false</code>
     */
    boolean forceUnlock();

    /**
     * Checks if the lock locked by any thread-uuid.
     *
     * @return <code>true</code> if locked otherwise <code>false</code>
     */
    boolean isLocked();

    /**
     * Checks if the lock is held by thread with defined thread.
     *
     * @param threadId Thread ID of locking thread
     * @return <code>true</code> if held by thread with given id
     *     otherwise <code>false</code>
     */
    boolean isHeldByThread(String threadId);

    /**
     * Number of holds on this lock by the current thread.
     *
     * @return holds or <code>0</code> if this lock is not held by current thread
     */
    int getHoldCount(String threadId);

    /**
     * Remaining time to live of the lock.
     *
     * @return time in milliseconds
     *     -2 if the lock does not exist.
     *     -1 if the lock exists but has no associated expire.
     */
    long remainTimeToLive();

}

