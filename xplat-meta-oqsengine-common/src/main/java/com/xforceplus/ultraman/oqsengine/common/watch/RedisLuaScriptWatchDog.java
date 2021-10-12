package com.xforceplus.ultraman.oqsengine.common.watch;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.common.timerwheel.TimeoutNotification;
import com.xforceplus.ultraman.oqsengine.common.timerwheel.TimerWheel;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * redis的lua脚本缓存监控器,使用SCRIPT.EXISTS来判断指定脚本的KEY是否存在. 如果不存在将将执行预期的方法重置脚本. 最终保证脚本一直有效.
 *
 * @author dongbin
 * @version 0.1 2021/09/23 10:41
 * @since 1.8
 */
public class RedisLuaScriptWatchDog implements Lifecycle {

    final Logger logger = LoggerFactory.getLogger(RedisLuaScriptWatchDog.class);

    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;
    // key是脚本sha, value是脚本.
    private ConcurrentMap<String, String> scripts;
    // 关注指定脚本的sha.
    private TimerWheel<String> timerWheel;
    private long checkTimeIntervalMs;

    /**
     * 以默认的检查间隔时间来构造watch dog.
     *
     * @param redisClient 操作redis客户端.
     */
    public RedisLuaScriptWatchDog(RedisClient redisClient) {
        this(redisClient, 60 * 1000);
    }

    /**
     * 构造一个redis lua的watch dog.
     *
     * @param redisClient         操作redis的客户端.
     * @param checkTimeIntervalMs 每个脚本的检查时间(毫秒).
     */
    public RedisLuaScriptWatchDog(RedisClient redisClient, long checkTimeIntervalMs) {
        this.checkTimeIntervalMs = checkTimeIntervalMs;

        this.connection = redisClient.connect();
        this.syncCommands = this.connection.sync();

        scripts = new ConcurrentHashMap<>();

        this.timerWheel = new TimerWheel<>(new ShaTimeoutNotification());

    }

    @PreDestroy
    @Override
    public void destroy() throws SQLException {
        this.connection.close();
    }

    /**
     * 关注某个脚本.保证脚本存活在redis缓存中.
     *
     * @param lua 目标lua脚本.
     * @return 脚本的load后sha.
     */
    public String watch(String lua) {
        String sha = this.syncCommands.scriptLoad(lua);

        addWatch(sha, lua);

        logger.info("Redis Lua Watch Dog starts to focus on scripts ({}).", lua);

        return sha;
    }

    /**
     * 忽略某个脚本,不再保证其存在于redis缓存中.
     *
     * @param sha 脚本的sha.
     */
    public void ignore(String sha) {
        timerWheel.remove(sha);

        scripts.remove(sha);
    }

    private void notExistLoad(String key, Supplier<String> scriptBuilder) {
        if (!exist(key)) {
            String lua = scriptBuilder.get();
            String sha = this.syncCommands.scriptLoad(lua);
            addWatch(sha, lua);

            logger.info("{} scipt lost, restore.", lua);
        }
    }

    private void addWatch(String key, String lua) {
        String oldLua = scripts.putIfAbsent(key, lua);
        if (oldLua != null) {
            timerWheel.remove(key);
        }
        timerWheel.add(key, checkTimeIntervalMs);
    }

    private boolean exist(String sha) {
        List<Boolean> results = this.syncCommands.scriptExists(sha);
        return results.get(0);
    }

    /**
     * 通知检查结点.
     */
    class ShaTimeoutNotification implements TimeoutNotification<String> {

        @Override
        public long notice(String sha) {
            if (syncCommands.isOpen()) {

                String lua = scripts.get(sha);
                if (lua != null) {
                    notExistLoad(sha, () -> lua);
                    return checkTimeIntervalMs;
                }

                return TimeoutNotification.OVERDUE;

            } else {

                // 连接失效,等待下次检查.
                return checkTimeIntervalMs;
            }
        }
    }

}
