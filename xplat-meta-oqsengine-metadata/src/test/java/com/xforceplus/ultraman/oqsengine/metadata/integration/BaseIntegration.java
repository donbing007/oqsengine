package com.xforceplus.ultraman.oqsengine.metadata.integration;

import static com.xforceplus.ultraman.test.tools.constant.ContainerEnvKeys.BOCP_GRPC_PORT;
import static com.xforceplus.ultraman.test.tools.constant.ContainerEnvKeys.BOCP_HOST;
import static com.xforceplus.ultraman.test.tools.constant.ContainerEnvKeys.BOCP_PORT;

import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.connect.MetaSyncGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.ExpireExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.recover.Constant;
import com.xforceplus.ultraman.test.tools.container.basic.MysqlContainer;
import com.xforceplus.ultraman.test.tools.container.basic.RedisContainer;
import com.xforceplus.ultraman.test.tools.container.module.BocpContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */

public class BaseIntegration {

    public static final boolean TEST_OPEN = true;

    protected RedisClient redisClient;

    protected EnhancedSyncExecutor enhancedSyncExecutor;

    protected StorageMetaManager storageMetaManager;

    protected DefaultCacheExecutor cacheExecutor;

    protected IRequestHandler requestHandler;

    protected EntityClassSyncClient entityClassSyncClient;

    protected ExecutorService executorService;


//    @BeforeClass
    public void beforeClass() {
        /*
         * init RedisClient
         */
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());
    }

//    @AfterClass
    public void afterClass() {

        redisClient.connect().sync().flushall();
        redisClient.shutdown();
        redisClient = null;
    }

    protected void init() throws InterruptedException {
        /*
         * init cacheExecutor
         */
        cacheExecutor = new DefaultCacheExecutor();

        ReflectionTestUtils.setField(cacheExecutor, "redisClient", redisClient);
        cacheExecutor.init();

        /*
         * init entityClassExecutor
         */
        enhancedSyncExecutor = new EnhancedSyncExecutor();

        ReflectionTestUtils.setField(enhancedSyncExecutor, "cacheExecutor", cacheExecutor);
        ReflectionTestUtils.setField(enhancedSyncExecutor, "expireExecutor", new ExpireExecutor());
        ReflectionTestUtils.setField(enhancedSyncExecutor, "eventBus", new EventBus() {

            @Override
            public void watch(EventType type, Consumer<Event> listener) {
                Assert.assertEquals(type, EventType.AUTO_FILL_UPGRADE);
            }

            @Override
            public void notify(Event event) {
                Assert.assertEquals(event.type(), EventType.AUTO_FILL_UPGRADE);
            }
        });

        enhancedSyncExecutor.start();


        /*
         * init requestHandler
         */
        GRpcParams grpcParams = Constant.grpcParamsConfig();

        RequestWatchExecutor requestWatchExecutor = requestWatchExecutor();

        requestHandler = requestHandler(requestWatchExecutor, grpcParams);
        entityClassSyncClient = entityClassSyncClient(grpcParams);
        /*
         * init entityClassManagerExecutor
         */
        executorService = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        storageMetaManager = new StorageMetaManager();
        ReflectionTestUtils.setField(storageMetaManager, "cacheExecutor", cacheExecutor);
        ReflectionTestUtils.setField(storageMetaManager, "requestHandler", requestHandler);
        ReflectionTestUtils.setField(storageMetaManager, "asyncDispatcher", executorService);

        entityClassSyncClient.start();
//
//        Thread.sleep(5000);
    }

    protected void destroy() {
        cacheExecutor.destroy();
        cacheExecutor = null;
        enhancedSyncExecutor.stop();
        entityClassSyncClient.stop();
    }



    private EntityClassSyncClient entityClassSyncClient(GRpcParams grpcParams) {

        MetaSyncGRpcClient metaSyncGrpcClient = new MetaSyncGRpcClient(System.getProperty(BOCP_HOST),
            Integer.parseInt(System.getProperty(BOCP_GRPC_PORT)));
        ReflectionTestUtils.setField(metaSyncGrpcClient, "grpcParams", grpcParams);

        EntityClassSyncClient entityClassSyncClient = new EntityClassSyncClient();

        ReflectionTestUtils.setField(entityClassSyncClient, "client", metaSyncGrpcClient);
        ReflectionTestUtils.setField(entityClassSyncClient, "requestHandler", requestHandler);
        ReflectionTestUtils.setField(entityClassSyncClient, "grpcParamsConfig", grpcParams);

        return entityClassSyncClient;
    }

    private RequestWatchExecutor requestWatchExecutor() {
        RequestWatchExecutor requestWatchExecutor = new RequestWatchExecutor();
        return requestWatchExecutor;
    }

    private IRequestHandler requestHandler(RequestWatchExecutor requestWatchExecutor, GRpcParams grpcParams) {
        IRequestHandler requestHandler = new SyncRequestHandler();


        executorService = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        ReflectionTestUtils.setField(requestHandler, "syncExecutor", enhancedSyncExecutor);
        ReflectionTestUtils.setField(requestHandler, "requestWatchExecutor", requestWatchExecutor);
        ReflectionTestUtils.setField(requestHandler, "grpcParams", grpcParams);
        ReflectionTestUtils.setField(requestHandler, "executorService", executorService);

        return requestHandler;
    }


}
