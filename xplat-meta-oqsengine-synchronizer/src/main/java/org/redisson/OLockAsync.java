package org.redisson;

import java.util.concurrent.TimeUnit;
import org.redisson.api.RFuture;

/**
 * olock.
 */
public interface OLockAsync {

    /**
     * Unlocks the lock independently of its state.
     *
     * @return <code>true</code> if lock existed and now unlocked
     * <code>false</code>
     */
    RFuture<Boolean> forceUnlockAsync();

    /**
     * Unlocks the lock. Throws {@link IllegalMonitorStateException}
     * if lock isn't locked by thread with specified <code>threadId</code>.
     *
     * @param threadId id of thread
     * @return void
     */
    RFuture<Void> unlockAsync(String threadId);

    /**
     * Tries to acquire the lock.
     *
     * @return <code>true</code> if lock acquired otherwise <code>false</code>
     */
    RFuture<Boolean> tryLockAsync(String threadId);

    /**
     * Tries to acquire the lock by thread with specified <code>threadId</code> and  <code>leaseTime</code>.
     * Waits up to defined <code>waitTime</code> if necessary until the lock became available.
     * Lock will be released automatically after defined <code>leaseTime</code> interval.
     *
     * @param threadId  id of thread
     * @param waitTime  time interval to acquire lock
     * @param leaseTime time interval after which lock will be released automatically
     * @param unit      the time unit of the {@code waitTime} and {@code leaseTime} arguments
     * @return <code>true</code> if lock acquired otherwise <code>false</code>
     */
    RFuture<Boolean> tryLockAsync(long waitTime, long leaseTime, TimeUnit unit, String threadId);

    /**
     * Checks if the lock locked by any thread.
     *
     * @return <code>true</code> if locked otherwise <code>false</code>
     */
    RFuture<Boolean> isLockedAsync();

    /**
     * Remaining time to live of the lock.
     *
     * @return time in milliseconds
     *     -2 if the lock does not exist.
     *     -1 if the lock exists but has no associated expire.
     */
    RFuture<Long> remainTimeToLiveAsync();

}