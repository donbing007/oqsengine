package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.xforceplus.ultraman.oqsengine.cdc.context.RunnerContext;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 03/2022.
 * 测试cdcConnector的重新连接.
 *
 * @since 1.8
 */
public class ReconnectCDCRunnerTest extends AbstractCdcHelper {

    private static ErrorBatchProcessor batchProcessor;
    private static ErrorCDCConnector errorCDCConnector;

    private RunnerContext runnerContext;

    @BeforeAll
    public static void beforeAll() throws Exception {
        batchProcessor = AbstractCdcHelper.initErrorBatchProcessor();
        errorCDCConnector = AbstractCdcHelper.initErrorCDCConnector();
    }

    @BeforeEach
    public void before() throws Exception {
        super.init(null, batchProcessor, errorCDCConnector);
        runnerContext = cdcRunner.getContext();
    }

    @AfterEach
    public void after() throws Exception {
        super.clear(true);
    }

    @AfterAll
    public static void afterAll() {
        try {
            InitializationHelper.destroy();
        } catch (Exception e) {

        }
    }

    @Test
    public void reNewConnectTest() {
        int fails = 0;
        int maxFails = 100;

        while (true) {
            if (batchProcessor.getRecovers() > 3) {
                if (batchProcessor.getRecovers() == errorCDCConnector.getRenewConnect()) {
                    break;
                }
            }

            TimeWaitUtils.wakeupAfter(3, TimeUnit.SECONDS);

            Assertions.assertTrue(fails < maxFails);

            fails++;
        }
    }
}
