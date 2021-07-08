package com.xforceplus.ultraman.oqsengine.common.mock;

import static com.xforceplus.ultraman.oqsengine.common.mock.EnvMockConstant.RUNNER_CORE_POOL_MAX_SIZE;
import static com.xforceplus.ultraman.oqsengine.common.mock.EnvMockConstant.RUNNER_CORE_POOL_SIZE;
import static com.xforceplus.ultraman.oqsengine.common.mock.EnvMockConstant.RUNNER_DEQUE_CAPACITY;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import io.lettuce.core.RedisClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class CommonInitialization implements BeanInitialization {

    private static volatile CommonInitialization instance = null;

    private DataSourcePackage dataSourcePackage;
    private RedisClient redisClient;
    private ExecutorService runner;

    private CommonInitialization() {

    }

    /**
     * 获取单例.
     */
    public static CommonInitialization getInstance() throws IllegalAccessException {
        if (null == instance) {
            synchronized (CommonInitialization.class) {
                if (null == instance) {
                    instance = new CommonInitialization();
                    instance.init();
                    InitializationHelper.add(instance);
                }
            }
        }
        return instance;
    }

    @Override
    public void init() throws IllegalAccessException {
        runner = new ThreadPoolExecutor(RUNNER_CORE_POOL_SIZE, RUNNER_CORE_POOL_MAX_SIZE, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(RUNNER_DEQUE_CAPACITY));
    }

    @Override
    public void clear() throws Exception {
        if (null != redisClient) {
            redisClient.connect().sync().flushall();
        }
    }

    @Override
    public void destroy() {
        if (null != redisClient) {
            redisClient.shutdown();
            redisClient = null;
        }

        if (null != dataSourcePackage) {
            dataSourcePackage.close();
            dataSourcePackage = null;
        }

        runner.shutdownNow();
        runner = null;

        instance = null;
    }

    /**
     * get datasourcePackage with lazy init.
     */
    public synchronized DataSourcePackage getDataSourcePackage(boolean showSql) {
        if (null == dataSourcePackage) {
            dataSourcePackage = DataSourceFactory.build(showSql);
        }
        return dataSourcePackage;
    }

    /**
     * get redisClient with lazy init.
     */
    public synchronized RedisClient getRedisClient() {
        if (null == redisClient) {
            redisClient = RedisClient.create(
                String.format("redis://%s:%s", System.getProperty(EnvMockConstant.REDIS_HOST),
                    System.getProperty(EnvMockConstant.REDIS_PORT)));
        }
        return redisClient;
    }

    public ExecutorService getRunner() {
        return runner;
    }
}
