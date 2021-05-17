package com.xforceplus.ultraman.oqsengine.metadata.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassSyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.ExpireExecutor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.Assert;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * test.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/17
 * @since 1.8
 */
public class RemoteBase {
    public RedisClient redisClient;

    public DefaultCacheExecutor cacheExecutor;

    public EntityClassSyncExecutor entityClassSyncExecutor;

    public StorageMetaManager storageMetaManager;

    public ExecutorService executorService;

    /**
     * 初始化.
     */
    public void init() {
        /*
         * init RedisClient
         */
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        /*
         * init cacheExecutor
         */
        cacheExecutor = new DefaultCacheExecutor();

        ReflectionTestUtils.setField(cacheExecutor, "redisClient", redisClient);
        ReflectionTestUtils.setField(cacheExecutor, "objectMapper", new ObjectMapper());
        cacheExecutor.init();

        /*
         * init entityClassExecutor
         */
        entityClassSyncExecutor = new EntityClassSyncExecutor();
        ReflectionTestUtils.setField(entityClassSyncExecutor, "cacheExecutor", cacheExecutor);
        ReflectionTestUtils.setField(entityClassSyncExecutor, "expireExecutor", new ExpireExecutor());
        ReflectionTestUtils.setField(entityClassSyncExecutor, "eventBus", new EventBus() {

            @Override
            public void watch(EventType type, Consumer<Event> listener) {
                Assert.assertEquals(type, EventType.AUTO_FILL_UPGRADE);
            }

            @Override
            public void notify(Event event) {
                Assert.assertEquals(event.type(), EventType.AUTO_FILL_UPGRADE);
            }
        });

        entityClassSyncExecutor.start();

    }

    public void initStorage(IRequestHandler requestHandler) {
        /*
         * init entityClassManagerExecutor
         */
        executorService = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        storageMetaManager = new StorageMetaManager();
        ReflectionTestUtils.setField(storageMetaManager, "cacheExecutor", cacheExecutor);
        ReflectionTestUtils.setField(storageMetaManager, "requestHandler", requestHandler);
        ReflectionTestUtils.setField(storageMetaManager, "asyncDispatcher", executorService);
    }

    /**
     * 清理.
     */
    public void clear() {
        cacheExecutor.destroy();
        cacheExecutor = null;

        redisClient.connect().sync().flushall();
        redisClient.shutdown();
        redisClient = null;

        entityClassSyncExecutor.stop();
    }

    public void clearRedis() {
        redisClient.connect().sync().flushall();
    }
}
