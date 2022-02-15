package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.common.timerwheel.ITimerWheel;
import com.xforceplus.ultraman.oqsengine.common.timerwheel.MultipleTimerWheel;
import com.xforceplus.ultraman.oqsengine.common.timerwheel.TimeoutNotification;
import com.xforceplus.ultraman.oqsengine.common.watch.RedisLuaScriptWatchDog;
import com.xforceplus.ultraman.oqsengine.lock.utils.Locker;
import com.xforceplus.ultraman.oqsengine.lock.utils.StateKeys;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.Arrays;
import java.util.List;
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

    private final Logger logger = LoggerFactory.getLogger(RedisResourceLocker.class);


    @Resource(name = "redisClient")
    private RedisClient redisClient;

    @Resource
    private RedisLuaScriptWatchDog redisLuaScriptWatchDog;

    private ITimerWheel<String> timerWheel;

    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;
    private String lockScriptSha;
    private String unLockScriptSha;

    private long ttlMs = 1000 * 30;
    /*
     * 默认的锁存在时间.
     */
    private String ttlMsString;
    // 续期间隔.比TTL时间缩短10%.
    private long renewalIntervalMs;


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

        ttlMsString = Long.toString(ttlMs);
        renewalIntervalMs = ttlMs - (long) (ttlMs * 0.1F);

        timerWheel = new MultipleTimerWheel(new LockHeartbeatNotification(syncCommands));
    }

    @PreDestroy
    @Override
    public void destroy() throws Exception {
        timerWheel.destroy();
        connection.close();
    }

    @Override
    protected void doLocks(Locker locker, StateKeys stateKeys) {
        String[] keys = stateKeys.getNoCompleteKeys();
        long size =
            syncCommands.evalsha(lockScriptSha, ScriptOutputType.INTEGER, keys, locker.getName(),
                ttlMsString);

        for (int i = 0; i < size; i++) {
            stateKeys.move();
            timerWheel.add(keys[i], renewalIntervalMs);
        }
    }

    @Override
    protected int[] doUnLocks(Locker locker, StateKeys stateKeys) {
        String[] keys = stateKeys.getNoCompleteKeys();
        /*
        返回值是一个列表,包含了一系列序号,从1开始.
        表示未解锁的key下标.
         */
        int[] failKeyIndex =
            ((List<Long>) (syncCommands.evalsha(unLockScriptSha, ScriptOutputType.MULTI, keys, locker.getName())))
            .stream().mapToInt(i -> i.intValue()).sorted().toArray();

        for (int i = 0; i < keys.length; i++) {
            if (Arrays.binarySearch(failKeyIndex, i) < 0) {
                // 序号不在错误列表中,可以清理.
                timerWheel.remove(keys[i]);
            }
        }

        return failKeyIndex;
    }

    @Override
    protected boolean doIsLocking(String key) {
        return syncCommands.exists(key) > 0;
    }

    // 锁心跳续期.
    class LockHeartbeatNotification implements TimeoutNotification<String> {

        private RedisCommands<String, String> syncCommands;

        public LockHeartbeatNotification(RedisCommands<String, String> syncCommands) {
            this.syncCommands = syncCommands;
        }

        @Override
        public long notice(String key) {
            boolean ok = false;
            try {
                ok = this.syncCommands.pexpire(key, ttlMs);
            } catch (Exception ex) {
                // 发生了异常,不确定锁是否还存活,不做续期但是重新放回计时器等待下次尝试.
                logger.error(ex.getMessage(), ex);

                return renewalIntervalMs;
            }

            if (ok) {

                if (logger.isDebugEnabled()) {
                    logger.debug("Successfully renewed the lock {}.", key);
                }

                return renewalIntervalMs;
            } else {

                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to renew for lock {}.", key);
                }

                return 0;
            }
        }
    }
}
