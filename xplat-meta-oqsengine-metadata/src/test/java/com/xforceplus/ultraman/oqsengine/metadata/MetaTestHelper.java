package com.xforceplus.ultraman.oqsengine.metadata;

import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManagerHolder;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockRequestHandler;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
@ExtendWith({RedisContainer.class})
public abstract class MetaTestHelper {

    protected IRequestHandler mockRequestHandler;

    protected void init() throws IllegalAccessException {
        if (null == mockRequestHandler) {
            mockRequestHandler();
            init(mockRequestHandler);

            mockRequestHandler.start();
        }
    }

    protected void init(IRequestHandler requestHandler) throws IllegalAccessException {
        mockRequestHandler = requestHandler;
        MockMetaManagerHolder.resetMetaManager(mockRequestHandler);
    }

    protected void destroy() throws Exception {
        mockRequestHandler.stop();
        mockRequestHandler = null;
        MockMetaManagerHolder.resetMetaManager(null);
        InitializationHelper.clearAll();
    }

    public void mockRequestHandler() throws IllegalAccessException {
        /*
         * init mockRequestHandler
         */
        mockRequestHandler = new MockRequestHandler();
        ReflectionTestUtils.setField(mockRequestHandler, "syncExecutor",
                            MetaInitialization.getInstance().getEntityClassSyncExecutor());
    }
}
