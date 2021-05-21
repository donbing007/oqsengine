package org.redisson;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.redisson.api.BatchOptions;
import org.redisson.api.BatchResult;
import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.convertor.IntegerReplayConvertor;
import org.redisson.client.protocol.decoder.MapValueDecoder;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.command.CommandBatchService;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.misc.RPromise;
import org.redisson.misc.RedissonPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * oqsBaseLock .
 */
public abstract class OqsBaseLock extends RedissonExpirable implements OLock {

    public static class ExpirationEntry {

        private final Map<String, Integer> threadIds = new LinkedHashMap<>();
        private volatile Timeout timeout;

        public ExpirationEntry() {
            super();
        }

        public synchronized void addThreadId(String threadUUID) {
            Integer counter = threadIds.get(threadUUID);
            if (counter == null) {
                counter = 1;
            } else {
                counter++;
            }
            threadIds.put(threadUUID, counter);
        }

        public synchronized boolean hasNoThreads() {
            return threadIds.isEmpty();
        }

        public synchronized String getFirstThreadId() {
            if (threadIds.isEmpty()) {
                return null;
            }
            return threadIds.keySet().iterator().next();
        }

        public synchronized void removeThreadId(String uuid) {
            Integer counter = threadIds.get(uuid);
            if (counter == null) {
                return;
            }
            counter--;
            if (counter == 0) {
                threadIds.remove(uuid);
            } else {
                threadIds.put(uuid, counter);
            }
        }

        public void setTimeout(Timeout timeout) {
            this.timeout = timeout;
        }

        public Timeout getTimeout() {
            return timeout;
        }

    }

    private static final Logger log = LoggerFactory.getLogger(org.redisson.RedissonBaseLock.class);

    private static final ConcurrentMap<String, ExpirationEntry> EXPIRATION_RENEWAL_MAP = new ConcurrentHashMap<>();
    protected long internalLockLeaseTime;

    final String id;
    final String entryName;

    final CommandAsyncExecutor commandExecutor;

    public OqsBaseLock(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
        this.commandExecutor = commandExecutor;
        this.id = commandExecutor.getConnectionManager().getId();
        this.internalLockLeaseTime = commandExecutor.getConnectionManager().getCfg().getLockWatchdogTimeout();
        this.entryName = id + ":" + name;
    }

    protected String getEntryName() {
        return entryName;
    }

    protected String getLockName(String threadUUID) {
        return id + ":" + threadUUID;
    }

    private void renewExpiration() {
        ExpirationEntry ee = EXPIRATION_RENEWAL_MAP.get(getEntryName());
        if (ee == null) {
            return;
        }

        Timeout task = commandExecutor.getConnectionManager().newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                ExpirationEntry ent = EXPIRATION_RENEWAL_MAP.get(getEntryName());
                if (ent == null) {
                    return;
                }
                String threadId = ent.getFirstThreadId();
                if (threadId == null) {
                    return;
                }

                RFuture<Boolean> future = renewExpirationAsync(threadId);
                future.onComplete((res, e) -> {
                    if (e != null) {
                        log.error("Can't update lock " + getRawName() + " expiration", e);
                        EXPIRATION_RENEWAL_MAP.remove(getEntryName());
                        return;
                    }

                    if (res) {
                        // reschedule itself
                        renewExpiration();
                    }
                });
            }
        }, internalLockLeaseTime / 3, TimeUnit.MILLISECONDS);

        ee.setTimeout(task);
    }

    protected void scheduleExpirationRenewal(String threadId) {
        ExpirationEntry entry = new ExpirationEntry();
        ExpirationEntry oldEntry = EXPIRATION_RENEWAL_MAP.putIfAbsent(getEntryName(), entry);
        if (oldEntry != null) {
            oldEntry.addThreadId(threadId);
        } else {
            entry.addThreadId(threadId);
            renewExpiration();
        }
    }

    protected RFuture<Boolean> renewExpirationAsync(String threadUUID) {
        return evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                        "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                        "return 1; " +
                        "end; " +
                        "return 0;",
                Collections.singletonList(getRawName()),
                internalLockLeaseTime, getLockName(threadUUID));
    }

    protected void cancelExpirationRenewal(String threadId) {
        ExpirationEntry task = EXPIRATION_RENEWAL_MAP.get(getEntryName());
        if (task == null) {
            return;
        }

        if (threadId != null) {
            task.removeThreadId(threadId);
        }

        if (threadId == null || task.hasNoThreads()) {
            Timeout timeout = task.getTimeout();
            if (timeout != null) {
                timeout.cancel();
            }
            EXPIRATION_RENEWAL_MAP.remove(getEntryName());
        }
    }

    protected <T> RFuture<T> evalWriteAsync(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params) {
        CommandBatchService executorService = createCommandBatchService();
        RFuture<T> result = executorService.evalWriteAsync(key, codec, evalCommandType, script, keys, params);
        if (commandExecutor instanceof CommandBatchService) {
            return result;
        }

        RPromise<T> r = new RedissonPromise<>();
        RFuture<BatchResult<?>> future = executorService.executeAsync();
        future.onComplete((res, ex) -> {
            if (ex != null) {
                r.tryFailure(ex);
                return;
            }

            r.trySuccess(result.getNow());
        });
        return r;
    }

    private CommandBatchService createCommandBatchService() {
        if (commandExecutor instanceof CommandBatchService) {
            return (CommandBatchService) commandExecutor;
        }

        MasterSlaveEntry entry = commandExecutor.getConnectionManager().getEntry(getRawName());
        BatchOptions options = BatchOptions.defaults()
                .syncSlaves(entry.getAvailableSlaves(), 1, TimeUnit.SECONDS);

        return new CommandBatchService(commandExecutor, options);
    }

    protected void acquireFailed(long waitTime, TimeUnit unit, String threadId) {
        get(acquireFailedAsync(waitTime, unit, threadId));
    }

    protected RFuture<Void> acquireFailedAsync(long waitTime, TimeUnit unit, String threadId) {
        return RedissonPromise.newSucceededFuture(null);
    }

    @Override
    public boolean isLocked() {
        return isExists();
    }

    @Override
    public RFuture<Boolean> isLockedAsync() {
        return isExistsAsync();
    }

    @Override
    public boolean isHeldByThread(String threadUUID) {
        RFuture<Boolean> future = commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.HEXISTS, getRawName(), getLockName(threadUUID));
        return get(future);
    }

    private static final RedisCommand<Integer> HGET = new RedisCommand<Integer>("HGET", new MapValueDecoder(), new IntegerReplayConvertor(0));

    public RFuture<Integer> getHoldCountAsync(String threadUUID) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, HGET, getRawName(), getLockName(threadUUID));
    }

    @Override
    public int getHoldCount(String threadUUID) {
        return get(getHoldCountAsync(threadUUID));
    }

    @Override
    public RFuture<Boolean> deleteAsync() {
        return forceUnlockAsync();
    }

    @Override
    public RFuture<Void> unlockAsync(String threadId) {
        RPromise<Void> result = new RedissonPromise<>();
        RFuture<Boolean> future = unlockInnerAsync(threadId);

        future.onComplete((opStatus, e) -> {
            cancelExpirationRenewal(threadId);

            if (e != null) {
                result.tryFailure(e);
                return;
            }

            if (opStatus == null) {
                IllegalMonitorStateException cause = new IllegalMonitorStateException("attempt to unlock lock, not locked by current thread by node id: "
                        + id + " thread-id: " + threadId);
                result.tryFailure(cause);
                return;
            }

            result.trySuccess(null);
        });

        return result;
    }

    protected abstract RFuture<Boolean> unlockInnerAsync(String threadId);
}
