package com.xforceplus.ultraman.oqsengine.boot.config;

import akka.actor.ActorSystem;
import com.xforceplus.ultraman.oqsengine.boot.config.redis.LettuceConfiguration;
import com.xforceplus.ultraman.oqsengine.boot.util.RedisConfigUtil;
import com.xforceplus.ultraman.oqsengine.common.lock.LocalResourceLocker;
import com.xforceplus.ultraman.oqsengine.common.lock.ResourceLocker;
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
    public LockStateService lockStateService(LettuceConfiguration configuration) {

        Config config = new Config();
        config.useSingleServer().setAddress(configuration.getUri());
        Config configCopy = new Config(config);
        String password = RedisConfigUtil.getRedisUrlPassword(configuration.getUri());
        if (!StringUtils.isBlank(password)) {
            configCopy.useSingleServer().setPassword(password);
        }
        ConnectionManager connectionManager = ConfigSupport.createConnectionManager(configCopy);
        RedissonObjectBuilder objectBuilder = null;
        CommandSyncService commandExecutor = new CommandSyncService(connectionManager, objectBuilder);
        return new LockStateServiceImpl(commandExecutor);
    }
}
