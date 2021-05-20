package org.redisson;

import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.client.RedisResponseTimeoutException;
import org.redisson.misc.RPromise;
import org.redisson.misc.RedissonPromise;
import org.redisson.misc.TransferListener;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

/**
 * oqs multi lock
 */
public class OqsMultiLock {

    class LockState {

        private final long newLeaseTime;
        private final long lockWaitTime;
        private final List<OqsLock> acquiredLocks;
        private final long waitTime;
        private final String threadId;
        private final long leaseTime;
        private final TimeUnit unit;

        private long remainTime;
        private long time = System.currentTimeMillis();
        private int failedLocksLimit;

        LockState(long waitTime, long leaseTime, TimeUnit unit, String threadId) {
            this.waitTime = waitTime;
            this.leaseTime = leaseTime;
            this.unit = unit;
            this.threadId = threadId;

            if (leaseTime != -1) {
                if (waitTime == -1) {
                    newLeaseTime = unit.toMillis(leaseTime);
                } else {
                    newLeaseTime = unit.toMillis(waitTime) * 2;
                }
            } else {
                newLeaseTime = -1;
            }

            remainTime = -1;
            if (waitTime != -1) {
                remainTime = unit.toMillis(waitTime);
            }
            lockWaitTime = calcLockWaitTime(remainTime);

            failedLocksLimit = failedLocksLimit();
            acquiredLocks = new ArrayList<>(locks.size());
        }

        void tryAcquireLockAsync(ListIterator<OqsLock> iterator, RPromise<Boolean> result) {
            if (!iterator.hasNext()) {
                checkLeaseTimeAsync(result);
                return;
            }

            OqsLock lock = iterator.next();
            RPromise<Boolean> lockAcquiredFuture = new RedissonPromise<Boolean>();
            if (waitTime == -1 && leaseTime == -1) {
                lock.tryLockAsync(threadId)
                        .onComplete(new TransferListener<Boolean>(lockAcquiredFuture));
            } else {
                long awaitTime = Math.min(lockWaitTime, remainTime);
                lock.tryLockAsync(awaitTime, newLeaseTime, TimeUnit.MILLISECONDS, threadId)
                        .onComplete(new TransferListener<Boolean>(lockAcquiredFuture));
            }

            lockAcquiredFuture.onComplete((res, e) -> {
                boolean lockAcquired = false;
                if (res != null) {
                    lockAcquired = res;
                }

                if (e instanceof RedisResponseTimeoutException) {
                    unlockInnerAsync(Arrays.asList(lock), threadId);
                }

                if (lockAcquired) {
                    acquiredLocks.add(lock);
                } else {
                    if (locks.size() - acquiredLocks.size() == failedLocksLimit()) {
                        checkLeaseTimeAsync(result);
                        return;
                    }

                    if (failedLocksLimit == 0) {
                        unlockInnerAsync(acquiredLocks, threadId).onComplete((r, ex) -> {
                            if (ex != null) {
                                result.tryFailure(ex);
                                return;
                            }

                            if (waitTime == -1) {
                                result.trySuccess(false);
                                return;
                            }

                            failedLocksLimit = failedLocksLimit();
                            acquiredLocks.clear();
                            // reset iterator
                            while (iterator.hasPrevious()) {
                                iterator.previous();
                            }

                            checkRemainTimeAsync(iterator, result);
                        });
                        return;
                    } else {
                        failedLocksLimit--;
                    }
                }

                checkRemainTimeAsync(iterator, result);
            });
        }

        private void checkLeaseTimeAsync(RPromise<Boolean> result) {
            if (leaseTime != -1) {
                AtomicInteger counter = new AtomicInteger(acquiredLocks.size());
                for (OqsLock rLock : acquiredLocks) {
                    RFuture<Boolean> future = rLock.expireAsync(unit.toMillis(leaseTime), TimeUnit.MILLISECONDS);
                    future.onComplete((res, e) -> {
                        if (e != null) {
                            result.tryFailure(e);
                            return;
                        }

                        if (counter.decrementAndGet() == 0) {
                            result.trySuccess(true);
                        }
                    });
                }
                return;
            }

            result.trySuccess(true);
        }

        private void checkRemainTimeAsync(ListIterator<OqsLock> iterator, RPromise<Boolean> result) {
            if (remainTime != -1) {
                remainTime += -(System.currentTimeMillis() - time);
                time = System.currentTimeMillis();
                if (remainTime <= 0) {
                    unlockInnerAsync(acquiredLocks, threadId).onComplete((res, e) -> {
                        if (e != null) {
                            result.tryFailure(e);
                            return;
                        }

                        result.trySuccess(false);
                    });
                    return;
                }
            }

            tryAcquireLockAsync(iterator, result);
        }

    }

    final List<OqsLock> locks = new ArrayList<>();

    /**
     * Creates instance with multiple {@link RLock} objects.
     * Each RLock object could be created by own Redisson instance.
     *
     * @param locks - array of locks
     */
    public OqsMultiLock(OqsLock... locks) {
        if (locks.length == 0) {
            throw new IllegalArgumentException("Lock objects are not defined");
        }
        this.locks.addAll(Arrays.asList(locks));
    }

    public OqsMultiLock(List<OqsLock> locks) {
        if (locks.size() == 0) {
            throw new IllegalArgumentException("Lock objects are not defined");
        }
        this.locks.addAll(locks);
    }

    public void lock(String threadId) {
        try {
            lockInterruptibly(threadId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void lock(String threadId, long leaseTime, TimeUnit unit) {
        try {
            lockInterruptibly(threadId, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public RFuture<Void> lockAsync(long leaseTime, TimeUnit unit, String threadId) {
        long baseWaitTime = locks.size() * 1500;
        long waitTime = -1;
        if (leaseTime == -1) {
            waitTime = baseWaitTime;
        } else {
            leaseTime = unit.toMillis(leaseTime);
            waitTime = leaseTime;
            if (waitTime <= 2000) {
                waitTime = 2000;
            } else if (waitTime <= baseWaitTime) {
                waitTime = ThreadLocalRandom.current().nextLong(waitTime / 2, waitTime);
            } else {
                waitTime = ThreadLocalRandom.current().nextLong(baseWaitTime, waitTime);
            }
        }

        RPromise<Void> result = new RedissonPromise<Void>();
        tryLockAsync(threadId, leaseTime, TimeUnit.MILLISECONDS, waitTime, result);
        return result;
    }

    protected void tryLockAsync(String threadId, long leaseTime, TimeUnit unit, long waitTime, RPromise<Void> result) {
        tryLockAsync(waitTime, leaseTime, unit, threadId).onComplete((res, e) -> {
            if (e != null) {
                result.tryFailure(e);
                return;
            }

            if (res) {
                result.trySuccess(null);
            } else {
                tryLockAsync(threadId, leaseTime, unit, waitTime, result);
            }
        });
    }

    public void lockInterruptibly(String threadId) throws InterruptedException {
        lockInterruptibly(threadId, -1, null);
    }

    public void lockInterruptibly(String threadId, long leaseTime, TimeUnit unit) throws InterruptedException {
        long baseWaitTime = locks.size() * 1500;
        long waitTime = -1;
        if (leaseTime == -1) {
            waitTime = baseWaitTime;
        } else {
            leaseTime = unit.toMillis(leaseTime);
            waitTime = leaseTime;
            if (waitTime <= 2000) {
                waitTime = 2000;
            } else if (waitTime <= baseWaitTime) {
                waitTime = ThreadLocalRandom.current().nextLong(waitTime / 2, waitTime);
            } else {
                waitTime = ThreadLocalRandom.current().nextLong(baseWaitTime, waitTime);
            }
        }

        while (true) {
            if (tryLock(threadId, waitTime, leaseTime, TimeUnit.MILLISECONDS)) {
                return;
            }
        }
    }

    protected void unlockInner(Collection<OqsLock> locks, String theadId) {
        System.out.println("unlock!!!!!");
        locks.stream()
                .forEach(x -> x.unlock(theadId));
    }

    protected RFuture<Void> unlockInnerAsync(Collection<OqsLock> locks, String threadId) {
        if (locks.isEmpty()) {
            return RedissonPromise.newSucceededFuture(null);
        }

        RPromise<Void> result = new RedissonPromise<Void>();
        AtomicInteger counter = new AtomicInteger(locks.size());
        for (OqsLock lock : locks) {
            lock.unlockAsync(threadId).onComplete((res, e) -> {
                if (e != null) {
                    result.tryFailure(e);
                    return;
                }

                if (counter.decrementAndGet() == 0) {
                    result.trySuccess(null);
                }
            });
        }
        return result;
    }

    public boolean tryLock(String threadId, long waitTime, TimeUnit unit) throws InterruptedException {
        return tryLock(threadId, waitTime, -1, unit);
    }

    protected int failedLocksLimit() {
        return 0;
    }

    public boolean tryLock(String threadId, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        long newLeaseTime = -1;
        if (leaseTime != -1) {
            if (waitTime == -1) {
                newLeaseTime = unit.toMillis(leaseTime);
            } else {
                newLeaseTime = unit.toMillis(waitTime) * 2;
            }
        }

        long time = System.currentTimeMillis();
        long remainTime = -1;
        if (waitTime != -1) {
            remainTime = unit.toMillis(waitTime);
        }
        long lockWaitTime = calcLockWaitTime(remainTime);

        int failedLocksLimit = failedLocksLimit();
        List<OqsLock> acquiredLocks = new ArrayList<>(locks.size());

        for (ListIterator<OqsLock> iterator = locks.listIterator(); iterator.hasNext(); ) {
            OqsLock lock = iterator.next();
            boolean lockAcquired;
            try {
                if (waitTime == -1 && leaseTime == -1) {
                    lockAcquired = lock.tryLock(threadId);
                } else {
                    long awaitTime = Math.min(lockWaitTime, remainTime);
                    lockAcquired = lock.tryLock(awaitTime, newLeaseTime, unit, threadId);
                }
            } catch (RedisResponseTimeoutException e) {
                unlockInner(Arrays.asList(lock), threadId);
                lockAcquired = false;
            } catch (Exception e) {
                lockAcquired = false;
            }

            if (lockAcquired) {
                acquiredLocks.add(lock);
            } else {
                if (locks.size() - acquiredLocks.size() == failedLocksLimit()) {
                    break;
                }

                if (failedLocksLimit == 0) {
                    unlockInner(acquiredLocks, threadId);
                    if (waitTime == -1) {
                        return false;
                    }
                    failedLocksLimit = failedLocksLimit();
                    acquiredLocks.clear();
                    // reset iterator
                    while (iterator.hasPrevious()) {
                        iterator.previous();
                    }
                } else {
                    failedLocksLimit--;
                }
            }

            if (remainTime != -1) {
                remainTime -= System.currentTimeMillis() - time;
                time = System.currentTimeMillis();
                if (remainTime <= 0) {
                    unlockInner(acquiredLocks, threadId);
                    return false;
                }
            }
        }

        if (leaseTime != -1) {
            //TODO set timeout
            acquiredLocks.stream()
                    .map(l -> l.expireAsync(unit.toMillis(leaseTime), TimeUnit.MILLISECONDS))
                    .forEach(f -> f.syncUninterruptibly());
        }

        return true;
    }

    public RFuture<Boolean> tryLockAsync(long waitTime, long leaseTime, TimeUnit unit, String threadId) {
        RPromise<Boolean> result = new RedissonPromise<Boolean>();
        OqsMultiLock.LockState state = new OqsMultiLock.LockState(waitTime, leaseTime, unit, threadId);
        state.tryAcquireLockAsync(locks.listIterator(), result);
        return result;
    }

    protected long calcLockWaitTime(long remainTime) {
        return remainTime;
    }

    public RFuture<Void> unlockAsync(String threadId) {
        return unlockInnerAsync(locks, threadId);
    }

    public void unlock(String threadId) {
        List<RFuture<Void>> futures = new ArrayList<>(locks.size());

        for (OqsLock lock : locks) {
            lock.unlock(threadId);
        }

        for (RFuture<Void> future : futures) {
            future.syncUninterruptibly();
        }
    }

    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    public RFuture<Boolean> forceUnlockAsync() {
        throw new UnsupportedOperationException();
    }

    public RFuture<Void> lockAsync(String threadId) {
        return lockAsync(-1, null, threadId);
    }

    public RFuture<Boolean> tryLockAsync(String threadId) {
        return tryLockAsync(-1, -1, null, threadId);
    }
}


