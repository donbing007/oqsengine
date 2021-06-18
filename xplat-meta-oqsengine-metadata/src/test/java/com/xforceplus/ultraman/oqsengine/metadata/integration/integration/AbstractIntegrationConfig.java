package com.xforceplus.ultraman.oqsengine.metadata.integration.integration;

import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.connect.MetaSyncGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.ExpireExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.mock.EnhancedSyncExecutor;
import com.xforceplus.ultraman.test.tools.constant.ContainerEnvKeys;
import com.xforceplus.ultraman.test.tools.container.basic.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.extension.ExtendWith;


/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
@ExtendWith({RedisContainer.class})
public abstract class AbstractIntegrationConfig {

    protected RedisClient redisClient;
    protected EnhancedSyncExecutor enhancedSyncExecutor;
    protected DefaultCacheExecutor cacheExecutor;
    protected IRequestHandler requestHandler;
    protected EntityClassSyncClient entityClassSyncClient;
    protected ExecutorService executorService;
    protected StorageMetaManager storageMetaManager;

    /**
     * 初始化.
     */
    public void initAll(boolean flag) throws IllegalAccessException {
        if (flag) {
            initRedis();
            initCacheExecutor();
            initSyncExecutor();
            initStorageMeta();

            enhancedSyncExecutor.start();
            entityClassSyncClient.start();
        }
    }

    /**
     * 销毁.
     */
    public void destroyAll(boolean flag) {
        if (flag) {
            entityClassSyncClient.stop();
            enhancedSyncExecutor.stop();

            destroyMetaStorage();
            destroySyncExecutor();
            destroyCacheExecutor();
            destroyRedis();
        }
    }

    private void initCacheExecutor() throws IllegalAccessException {
        /*
         * init cacheExecutor
         */
        cacheExecutor = new DefaultCacheExecutor();
        Collection<Field> fs = ReflectionUtils.printAllMembers(cacheExecutor);
        ReflectionUtils.reflectionFieldValue(fs, "redisClient", cacheExecutor, redisClient);
        cacheExecutor.init();
    }

    private void initSyncExecutor() throws IllegalAccessException {
        /*
         * init entityClassExecutor
         */
        enhancedSyncExecutor = new EnhancedSyncExecutor();
        Collection<Field> fs = ReflectionUtils.printAllMembers(enhancedSyncExecutor);
        ReflectionUtils.reflectionFieldValue(fs, "cacheExecutor", enhancedSyncExecutor, cacheExecutor);
        ReflectionUtils.reflectionFieldValue(fs, "expireExecutor", enhancedSyncExecutor, new ExpireExecutor());
        ReflectionUtils.reflectionFieldValue(fs, "eventBus", enhancedSyncExecutor, new EventBus() {

            @Override
            public void watch(EventType type, Consumer<Event> listener) {
                if (!type.equals(EventType.AUTO_FILL_UPGRADE)) {
                    throw new RuntimeException("assert type failed, type should be AUTO_FILL_UPGRADE");
                }
            }

            @Override
            public void notify(Event event) {
                if (!event.type().equals(EventType.AUTO_FILL_UPGRADE)) {
                    throw new RuntimeException("assert type failed, type should be AUTO_FILL_UPGRADE");
                }
            }
        });
    }

    private void initStorageMeta() throws IllegalAccessException {
        /*
         * init requestHandler
         */
        GRpcParams grpcParams = grpcParamsConfig();

        requestHandler(requestWatchExecutor(), grpcParams);
        entityClassSyncClient(grpcParams);

        /*
         * init entityClassManagerExecutor
         */
        executorService = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        storageMetaManager = new StorageMetaManager();

        Collection<Field> fs = ReflectionUtils.printAllMembers(storageMetaManager);
        ReflectionUtils.reflectionFieldValue(fs, "cacheExecutor", storageMetaManager, cacheExecutor);
        ReflectionUtils.reflectionFieldValue(fs, "requestHandler", storageMetaManager, requestHandler);
        ReflectionUtils.reflectionFieldValue(fs, "asyncDispatcher", storageMetaManager, executorService);

    }

    private void initRedis() {
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());
    }

    private void entityClassSyncClient(GRpcParams grpcParams) throws IllegalAccessException {

        MetaSyncGRpcClient metaSyncGrpcClient = new MetaSyncGRpcClient(System.getProperty(ContainerEnvKeys.BOCP_HOST),
            Integer.parseInt(System.getProperty(ContainerEnvKeys.BOCP_GRPC_PORT)));

        Collection<Field> fs = ReflectionUtils.printAllMembers(metaSyncGrpcClient);
        ReflectionUtils.reflectionFieldValue(fs, "grpcParams", metaSyncGrpcClient, grpcParams);

        entityClassSyncClient = new EntityClassSyncClient();
        Collection<Field> fc = ReflectionUtils.printAllMembers(entityClassSyncClient);
        ReflectionUtils.reflectionFieldValue(fc, "client", entityClassSyncClient, metaSyncGrpcClient);
        ReflectionUtils.reflectionFieldValue(fc, "requestHandler", entityClassSyncClient, requestHandler);
        ReflectionUtils.reflectionFieldValue(fc, "grpcParamsConfig", entityClassSyncClient, grpcParams);
    }

    private RequestWatchExecutor requestWatchExecutor() {
        return new RequestWatchExecutor();
    }

    private void requestHandler(RequestWatchExecutor requestWatchExecutor, GRpcParams grpcParams)
        throws IllegalAccessException {
        requestHandler = new SyncRequestHandler();

        executorService = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        Collection<Field> fs = ReflectionUtils.printAllMembers(requestHandler);
        ReflectionUtils.reflectionFieldValue(fs, "syncExecutor", requestHandler, enhancedSyncExecutor);
        ReflectionUtils.reflectionFieldValue(fs, "requestWatchExecutor", requestHandler, requestWatchExecutor);
        ReflectionUtils.reflectionFieldValue(fs, "grpcParams", requestHandler, grpcParams);
        ReflectionUtils.reflectionFieldValue(fs, "executorService", requestHandler, executorService);
    }

    /**
     * grpc 配置.
     */
    private GRpcParams grpcParamsConfig() {
        GRpcParams grpcParamsConfig = new GRpcParams();
        grpcParamsConfig.setDefaultDelayTaskDuration(30_000);
        grpcParamsConfig.setKeepAliveSendDuration(5_000);
        grpcParamsConfig.setReconnectDuration(5_000);
        grpcParamsConfig.setDefaultHeartbeatTimeout(30_000);
        grpcParamsConfig.setMonitorSleepDuration(1_000);

        return grpcParamsConfig;
    }


    private void destroyCacheExecutor() {
        cacheExecutor = null;
    }

    private void destroySyncExecutor() {
        enhancedSyncExecutor.clear();
        enhancedSyncExecutor = null;
    }

    private void destroyMetaStorage() {
        storageMetaManager = null;
    }

    private void destroyRedis() {
        redisClient.connect().sync().flushall();
        redisClient.shutdown();
        redisClient = null;
    }
}
