package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.watch.RedisLuaScriptWatchDog;
import com.xforceplus.ultraman.oqsengine.lock.utils.Locker;
import com.xforceplus.ultraman.oqsengine.lock.utils.StateKeys;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 一个redisson lock锁的包装实现.
 *
 * @author dongbin
 * @version 0.1 2021/08/10 18:41
 * @since 1.8
 */
public class RedisResourceLocker extends AbstractResourceLocker implements Lifecycle {

    final Logger logger = LoggerFactory.getLogger(RedisResourceLocker.class);

    /*
    最小可接受的超时时间.
     */
    private static final long MIN_TTL_MS = 1000 * 30;

    /*
    批量加锁lua脚本.使用一个hash结构来记录锁,如下.
    {"locker": "Id", "acc", 1}
    locker 为加锁者的标识字符串,必须保证直到解锁期间不能改变.
    acc    同一个加锁者重入的次数.

    这段脚本其保证要么都加锁成功,要么没有任何改变.
    newKeys 为此新增加的锁key.
    oldKeys 为当前加锁者重入的锁key.
    这两个key的数组将在加锁失败时用以恢复.
    newKeys将被删除, oldKeys的acc值将被减1.
    ARGV[1] = 加锁者
    ARGV[2] = 加锁TTL
    失败返回当前停留的位置,从0开始.
     */
    private static final String LOCK_SCRIPT =
        "local newLocker = ARGV[1];"
            + "local ttl = ARGV[2];"
            + "local curor = 0;"
            + "for i=1, #KEYS, 1 do"
            + "  local hasLocked = redis.call('EXISTS', KEYS[i]);"
            + "  if hasLocked == 1 then"
            + "    local locker = redis.call('HGET', KEYS[i], 'locker');"
            + "    if (locker == newLocker) then"
            + "      redis.call('HINCRBY', KEYS[i], 'acc', 1);"
            + "      redis.call('PEXPIRE', KEYS[i], ttl);"
            + "    else"
            + "     return curor;"
            + "    end;"
            + "  else"
            + "    redis.call('HMSET', KEYS[i], 'locker', newLocker, 'acc', 1);"
            + "    redis.call('PEXPIRE', KEYS[i], ttl);"
            + "  end;"
            + "  curor = curor + 1;"
            + "end;"
            + "return curor;";

    /*
    会检查当前需要解除的锁是否都是当前加锁者持有,否则解锁失败.
    会回滚到解锁之前的锁定状态.
    ARGV[1] = 解锁者
    返回一个数组,数组每一个元素都是一个key的下标(从0开始)
    表示未成功解锁的key下标.
     */
    private static final String UNLOCK_SCRIPT =
        "local freeLocker = ARGV[1];"
            + "local failKeyIndexPoint = 1;"
            + "local failKeyIndex = {};"
            + "for i=1, #KEYS, 1 do"
            + "  local hasLocked = redis.call('EXISTS', KEYS[i]);"
            + "  if hasLocked == 1 then"
            + "    local locker = redis.call('HGET', KEYS[i], 'locker');"
            + "    if locker ~= freeLocker then"
            + "      failKeyIndex[failKeyIndexPoint] = i - 1;"
            + "      failKeyIndexPoint = failKeyIndexPoint + 1;"
            + "    else"
            + "      local acc = redis.call('HINCRBY', KEYS[i], 'acc', -1);"
            + "      if acc == 0 then"
            + "        redis.call('DEL', KEYS[i]);"
            + "      end;"
            + "    end;"
            + "  end;"
            + "end;"
            + "return failKeyIndex;";

    @Resource(name = "redisClient")
    private RedisClient redisClient;

    @Resource
    private RedisLuaScriptWatchDog redisLuaScriptWatchDog;

    private ExecutorService worker;

    /*
    KEY = 加锁键.
    Value = 记录加锁者和最后一次的续期时间.
     */
    private Map<String, LockInfo> liveLocks;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;
    private StatefulRedisConnection<String, String> watchDogconnection;
    private RedisAsyncCommands<String, String> watchDogCommands;
    private String lockScriptSha;
    private String unLockScriptSha;
    private volatile boolean running;
    private volatile boolean watchDagRunning;

    /*
     * 锁存在时间.
     */
    private long ttlMs;
    /*
    续期间隔,比锁存在时间少20%时间.
     */
    private long renewalMs;
    /*
     * 存在时间的字符串表示.
     */
    private String ttlMsString;


    public RedisResourceLocker() {
    }

    public RedisResourceLocker(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public void setTtlMs(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    public long getTtlMs() {
        return ttlMs;
    }

    @PostConstruct
    @Override
    public void init() throws Exception {
        if (redisClient != null) {
            connection = redisClient.connect();
            syncCommands = connection.sync();

            watchDogconnection = redisClient.connect();
            watchDogCommands = watchDogconnection.async();
            watchDogCommands.setAutoFlushCommands(false);

        } else {
            throw new IllegalStateException("Invalid redisClient.");
        }

        if (redisLuaScriptWatchDog != null) {
            lockScriptSha = redisLuaScriptWatchDog.watch(LOCK_SCRIPT);
            unLockScriptSha = redisLuaScriptWatchDog.watch(UNLOCK_SCRIPT);
        } else {
            lockScriptSha = syncCommands.scriptLoad(LOCK_SCRIPT);
            unLockScriptSha = syncCommands.scriptLoad(UNLOCK_SCRIPT);
        }

        if (ttlMs < MIN_TTL_MS) {
            ttlMs = MIN_TTL_MS;
        }

        ttlMsString = Long.toString(ttlMs);
        renewalMs = ttlMs - ((long) (ttlMs * 0.2F));

        this.liveLocks = new ConcurrentHashMap();

        running = true;
        watchDagRunning = false;

        worker = Executors.newFixedThreadPool(1, ExecutorHelper.buildNameThreadFactory("redis-lock-watchdog"));
        worker.submit(new WatchDogTask());
    }

    @PreDestroy
    @Override
    public void destroy() throws Exception {
        running = false;

        // 等待watchDao被关闭.
        while (watchDagRunning) {
            TimeUnit.MILLISECONDS.sleep(10);
        }

        cleanAllLock();

        connection.close();
        watchDogconnection.close();

        ExecutorHelper.shutdownAndAwaitTermination(worker);
    }

    // 最后清理掉生存的锁.
    private void cleanAllLock() {
        Map<Locker, List<LockInfo>> groupLockInfos =
            this.liveLocks.values().stream().collect(Collectors.groupingBy(l -> l.locker));
        StateKeys stateKeys;
        for (Map.Entry<Locker, List<LockInfo>> entry : groupLockInfos.entrySet()) {
            String[] keys = entry.getValue().stream().map(lockInfo -> lockInfo.getLockKey()).toArray(String[]::new);
            stateKeys = new StateKeys(keys);

            doPriveUnLocks(entry.getKey(), stateKeys);

            keys = null;
            stateKeys = null;
        }
    }

    @Override
    protected void doLocks(Locker locker, StateKeys stateKeys) {
        if (!running) {
            throw new IllegalStateException("It has been shut down.");
        }

        String[] keys = stateKeys.getNoCompleteKeys();
        long size =
            syncCommands.evalsha(lockScriptSha, ScriptOutputType.INTEGER, keys, locker.getName(),
                ttlMsString);

        long now = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            stateKeys.move();

            liveLocks.put(keys[i], new LockInfo(keys[i], now, locker));
        }
    }

    @Override
    protected int[] doUnLocks(Locker locker, StateKeys stateKeys) {
        if (!running) {
            throw new IllegalStateException("It has been shut down.");
        }

        return doPriveUnLocks(locker, stateKeys);
    }

    @Override
    protected boolean doIsLocking(String key) {
        if (!liveLocks.containsKey(key)) {

            return syncCommands.exists(key) > 0;

        }

        return true;
    }

    // 实际解锁实现.
    private int[] doPriveUnLocks(Locker locker, StateKeys stateKeys) {

        String[] keys = stateKeys.getNoCompleteKeys();
        /*
        返回值是一个列表,包含了一系列序号,从1开始.
        表示未解锁的key下标.
         */
        int[] failKeyIndex =
            ((List<Long>) (syncCommands.evalsha(unLockScriptSha, ScriptOutputType.MULTI, keys, locker.getName())))
                .stream().mapToInt(i -> i.intValue()).sorted().toArray();

        // 不再需要关注的锁.
        int keyLen = keys.length;
        for (int i = 0; i < keyLen; i++) {
            liveLocks.remove(keys[i]);
        }

        return failKeyIndex;
    }

    // 记录加锁信息.
    private static class LockInfo {
        private String lockKey;
        private long lastRenewalTimeMs;
        private Locker locker;

        public LockInfo(String lockKey, long lastRenewalTimeMs, Locker locker) {
            this.lockKey = lockKey;
            this.lastRenewalTimeMs = lastRenewalTimeMs;
            this.locker = locker;
        }

        public String getLockKey() {
            return lockKey;
        }

        public long getLastRenewalTimeMs() {
            return lastRenewalTimeMs;
        }

        public void setLastRenewalTimeMs(long lastRenewalTimeMs) {
            this.lastRenewalTimeMs = lastRenewalTimeMs;
        }

        public Locker getLocker() {
            return locker;
        }
    }

    /*
    为成功加锁的key进行续期.
     */
    private class WatchDogTask implements Runnable {

        @Override
        public void run() {

            watchDagRunning = true;

            int commandSize = 0;
            long sleepMs = 100;

            if (logger.isDebugEnabled()) {
                logger.debug("Watchdog checks at a frequency of {} milliseconds.", sleepMs);
            }

            Map<String, RedisFuture<Boolean>> ackMap = new HashMap<>();
            try {
                while (running) {

                    ackMap.clear();

                    for (LockInfo lockInfo : liveLocks.values()) {

                        long nowMs = System.currentTimeMillis();

                        if (needRenewal(lockInfo.getLockKey(), nowMs, lockInfo.getLastRenewalTimeMs())) {
                            ackMap.put(lockInfo.getLockKey(), watchDogCommands.pexpire(lockInfo.getLockKey(), ttlMs));

                            if (logger.isDebugEnabled()) {
                                logger.debug("Renewal lock {}({}ms).",
                                    lockInfo.getLockKey(), nowMs - lockInfo.getLastRenewalTimeMs());
                            }

                            lockInfo.setLastRenewalTimeMs(nowMs);

                            commandSize++;
                        }
                    }

                    if (commandSize > 0) {
                        watchDogCommands.flushCommands();
                    }


                    for (Map.Entry<String, RedisFuture<Boolean>> entry : ackMap.entrySet()) {
                        RedisFuture<Boolean> future = entry.getValue();
                        String lockKey = entry.getKey();

                        if (!future.get()) {
                            // 续期失败,删除本地锁.

                        }
                    }

                    commandSize = 0;
                    LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(sleepMs));

                }
            } catch (Throwable ex) {

                logger.error(ex.getMessage(), ex);

            } finally {

                watchDagRunning = false;

            }
        }

        private boolean needRenewal(String key, long nowMs, long lastRenewalTimeMs) {
            if (liveLocks.containsKey(key)) {
                return nowMs - lastRenewalTimeMs >= renewalMs;
            } else {
                return false;
            }
        }
    }
}
