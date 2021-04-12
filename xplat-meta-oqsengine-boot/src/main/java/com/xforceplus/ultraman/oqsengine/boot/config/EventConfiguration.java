package com.xforceplus.ultraman.oqsengine.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.event.DefaultEventBus;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.storage.EventStorage;
import com.xforceplus.ultraman.oqsengine.event.storage.MemoryEventStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.CacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.CacheEventService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.RedisEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.RedisEventService;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

/**
 * @author dongbin
 * @version 0.1 2021/3/24 15:54
 * @since 1.8
 */
@Configuration
public class EventConfiguration {

    @Bean("eventBus")
    public EventBus eventBus(EventStorage eventStorage, ExecutorService eventWorker) {
        return new DefaultEventBus(eventStorage, eventWorker);
    }

    @Bean("eventStorage")
    public EventStorage eventStorage() {
        return new MemoryEventStorage();
    }

    @Bean
    public CacheEventHandler cacheEventHandler(RedisClient redisClient,
                                               ObjectMapper objectMapper,
                                               @Value("${cache.event.expire:0}") long expire) {
        return new RedisEventHandler(redisClient, objectMapper, expire);
    }

    @Bean
    public CacheEventService cacheEventService(CacheEventHandler cacheEventHandler) {
        return new RedisEventService(cacheEventHandler);
    }
}
