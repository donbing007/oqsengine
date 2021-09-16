package com.xforceplus.ultraman.oqsengine.boot.config;

import akka.actor.ActorSystem;
import com.xforceplus.ultraman.oqsengine.boot.config.redis.RedisConfiguration;
import com.xforceplus.ultraman.oqsengine.boot.util.RedisConfigUtil;
import com.xforceplus.ultraman.oqsengine.lock.LocalResourceLocker;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.LockStateService;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.impl.LockStateServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.redisson.command.CommandSyncService;
import org.redisson.config.Config;
import org.redisson.config.ConfigSupport;
import org.redisson.connection.ConnectionManager;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 锁配置.
 *
 * @author xujia 2020/11/27
 * @since : 1.8
 */
@Configuration
public class LockConfiguration {

    @Bean
    public ResourceLocker locker() {
        return new LocalResourceLocker();
    }

    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create();
    }

    /**
     * lockstate service init.
     *
     * @param configuration configuration
     * @return lock state service
     */
    @Bean
    public LockStateService lockStateService(RedisConfiguration configuration) {

        Config config = new Config();
        config.useSingleServer().setAddress(configuration.uriWithStateDb());
        Config configCopy = new Config(config);
        String password = RedisConfigUtil.getRedisUrlPassword(configuration.uriWithStateDb());
        if (!StringUtils.isBlank(password)) {
            if (password.startsWith(":")) {
                password = password.substring(1);
            }
            configCopy.useSingleServer().setPassword(password);
        }
        ConnectionManager connectionManager = ConfigSupport.createConnectionManager(configCopy);
        RedissonObjectBuilder objectBuilder = null;
        CommandSyncService commandExecutor = new CommandSyncService(connectionManager, objectBuilder);
        return new LockStateServiceImpl(commandExecutor);
    }
}
