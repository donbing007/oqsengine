package com.xforceplus.ultraman.oqsengine.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.event.DefaultEventBus;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.storage.MemoryEventStorage;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
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
 * desc :.
 * name : InitBase
 *
 * @author : xujia 2021/4/7
 * @since : 1.8
 */
public class InitBase {
    public RedisClient redisClient;

    public DefaultCacheExecutor cacheExecutor;

    public EntityClassSyncExecutor entityClassSyncExecutor;

    public StorageMetaManager storageMetaManager;

    public ExecutorService executorService;

    private final IRequestHandler requestHandler;

    public InitBase(IRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

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

        /*
         * init requestHandler
         */
        ReflectionTestUtils.setField(requestHandler, "syncExecutor", entityClassSyncExecutor);

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
