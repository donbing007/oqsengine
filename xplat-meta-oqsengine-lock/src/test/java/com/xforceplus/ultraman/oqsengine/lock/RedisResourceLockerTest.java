package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 基于redisson的资源锁封装测试.
 *
 * @author dongbin
 * @version 0.1 2021/08/10 19:08
 * @since 1.8
 */
@ExtendWith({RedisContainer.class})
public class RedisResourceLockerTest extends AbstractResourceLockerTest {

    private RedisClient redisClient;
    private RedisResourceLocker locker;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {

        redisClient = CommonInitialization.getInstance().getRedisClient();

        locker = new RedisResourceLocker(redisClient);
        locker.init();
    }

    /**
     * 清理.
     */
    @AfterEach
    public void after() throws Exception {
        locker.destroy();

        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    @Override
    public ResourceLocker getLocker() {
        return locker;
    }

    @Override
    public MultiResourceLocker getMultiLocker() {
        return this.locker;
    }
}