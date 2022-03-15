package com.xforceplus.ultraman.oqsengine.metadata.cache;

import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
@ExtendWith({RedisContainer.class})
public class DefaultCacheExecutorTest {

    private CacheExecutor cacheExecutor;

    @BeforeEach
    public void before() throws Exception {
        cacheExecutor = MetaInitialization.getInstance().getCacheExecutor();
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }
}
