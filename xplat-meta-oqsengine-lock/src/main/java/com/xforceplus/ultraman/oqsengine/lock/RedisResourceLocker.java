package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.common.watch.RedisLuaScriptWatchDog;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

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
    加锁成功返回1, 不成功返回0.
     */
    private static final String LOCK_SCRIPT =
        "local newLocker = ARGV[1];"
        + "local ttl = ARGV[2];"
        + "local newKeys = {};"
        + "local newKeysPoint = 1;"
        + "local oldKeys = {};"
        + "local oldKeysPoint = 1;"
        + "for i=1, #KEYS, 1 do "
        + "local hasLocked = redis.call('EXISTS', KEYS[i]);"
        + "  if hasLocked == 1 then"
        + "    local locker = redis.call('HGET', KEYS[i], 'locker');"
        + "    if (locker == newLocker) then "
        + "      redis.call('HINCRBY', KEYS[i], 'acc', 1);"
        + "      redis.call('PEXPIRE', KEYS[i], ttl);"
        + "      oldKeys[oldKeysPoint] = KEYS[i];"
        + "      oldKeysPoint = oldKeysPoint + 1;"
        + "    else"
        + "     for k = 1, newKeysPoint - 1 do"
        + "       redis.call('DEL', newKeys[k]);"
        + "     end"
        + "     for k = 1, oldKeysPoint - 1 do "
        + "       redis.call('HINCRBY', KEYS[i], 'acc', -1);"
        + "     end"
        + "     return 0;"
        + "    end"
        + "  else"
        + "    redis.call('HMSET', KEYS[i], 'locker', newLocker, 'acc', 1);"
        + "    redis.call('PEXPIRE', KEYS[i], ttl);"
        + "    newKeys[newKeysPoint] = KEYS[i];"
        + "    newKeysPoint = newKeysPoint + 1;"
        + "  end;"
        + "end;"
        + "return 1;";

    /*
    会检查当前需要解除的锁是否都是当前加锁者持有,否则解锁失败.
    会回滚到解锁之前的锁定状态.
    ARGV[1] = 解锁者
    成功返回1,不成功返回0.
     */
    private static final String UNLOCK_SCRIPT =
        "local freeLocker = ARGV[1];"
            + "for i=1, #KEYS, 1 do"
            + "  local hasLocked = redis.call('EXISTS', KEYS[i]);"
            + "  if hasLocked == 1 then"
            + "    local locker = redis.call('HGET', KEYS[i], 'locker');"
            + "    if locker ~= freeLocker then"
            + "      return 0;"
            + "    end;"
            + "  end;"
            + "end;"
            + "for i=1, #KEYS, 1 do"
            + "  local acc = redis.call('HINCRBY', KEYS[i], 'acc', -1);"
            + "  if acc == 0 then"
            + "    redis.call('DEL', KEYS[i]);"
            + "  end;"
            + "end;"
            + "return 1;";



    @Resource(name = "redisClient")
    private RedisClient redisClient;

    @Resource
    private RedisLuaScriptWatchDog redisLuaScriptWatchDog;

    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;
    private String lockScriptSha;
    private String unLockScriptSha;
    /*
     * 默认的锁存在时间.
     */
    private static String TTL = Long.toString(1000 * 60 * 10);

    public RedisResourceLocker() {
    }

    public RedisResourceLocker(RedisClient redisClient) {
        this.redisClient = redisClient;
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
    }

    @PreDestroy
    @Override
    public void destroy() throws Exception {
        connection.close();
    }

    @Override
    protected boolean doLock(Locker locker, String key) {
        return doLocks(locker, key);
    }

    @Override
    protected boolean doLocks(Locker locker, String... keys) {
        return syncCommands.evalsha(lockScriptSha, ScriptOutputType.BOOLEAN, keys, locker.getName(), TTL);
    }

    @Override
    protected boolean doUnLock(Locker locker, String key) {
        return doUnLocks(locker, key);
    }

    @Override
    protected boolean doUnLocks(Locker locker, String... keys) {
        return syncCommands.evalsha(unLockScriptSha, ScriptOutputType.BOOLEAN, keys, locker.getName());
    }

    @Override
    protected boolean doIsLocking(String key) {
        return syncCommands.exists(key) > 0;
    }
}
