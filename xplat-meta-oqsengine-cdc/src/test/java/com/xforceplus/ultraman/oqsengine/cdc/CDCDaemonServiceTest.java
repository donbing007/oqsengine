package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * desc :
 * name : CDCDaemonServiceTest.
 *
 * @author : xujia 2020/11/5
 * @since : 1.8
 */
public class CDCDaemonServiceTest extends AbstractCDCTestHelper {

    private CDCDaemonService cdcDaemonService;

    private boolean isTest = true;

    private MockRedisCallbackService testCallbackService;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        if (isTest) {
            super.init(true);
            cdcDaemonService = initDaemonService();
            cdcDaemonService.init();
        }
    }

    /**
     * 清理.
     */
    @AfterEach
    public void after() throws Exception {
        if (isTest) {
            super.destroy(true);
        }
    }

    @Test
    public void binlogSyncTest() throws InterruptedException {
        if (isTest) {
            Thread.sleep(10_000);
        }
    }
}
