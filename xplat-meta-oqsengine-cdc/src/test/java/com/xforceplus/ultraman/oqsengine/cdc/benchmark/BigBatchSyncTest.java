package com.xforceplus.ultraman.oqsengine.cdc.benchmark;

import com.xforceplus.ultraman.oqsengine.cdc.CDCAbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerRunner;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.getEntityClass;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

/**
 * desc :
 * name : BigBatchSyncTest
 *
 * @author : xujia
 * date : 2020/11/23
 * @since : 1.8
 */
public class BigBatchSyncTest extends CDCAbstractContainer {
    final Logger logger = LoggerFactory.getLogger(BigBatchSyncTest.class);

    private static int expectedSize = 0;
    private static int maxTestSize = 10;

    private ConsumerRunner consumerRunner;

    private MockRedisCallbackService mockRedisCallbackService;

    @BeforeClass
    public static void beforeClass() {
        ContainerStarter.startMysql();
        ContainerStarter.startManticore();
        ContainerStarter.startRedis();
        ContainerStarter.startCannal();
    }

    @AfterClass
    public static void afterClass() {
        ContainerStarter.reset();
    }

    @Before
    public void before() throws Exception {
        consumerRunner = initConsumerRunner();
        consumerRunner.start();
    }

    @After
    public void after() throws SQLException {
        consumerRunner.shutdown();
        clear();
        closeAll();
    }

    private ConsumerRunner initConsumerRunner() throws Exception {
        ConsumerService consumerService = initAll(false);
        CDCMetricsService cdcMetricsService = new CDCMetricsService();
        mockRedisCallbackService = new MockRedisCallbackService(commitIdStatusService);
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

        return new ConsumerRunner(consumerService, cdcMetricsService, singleCDCConnector);
    }

    @Test
    public void test() throws InterruptedException, SQLException {
        initData();

        boolean isStartUpdate = false;
        long start = 0;
        long duration = 0;
        while (true) {
            if (!isStartUpdate && mockRedisCallbackService.getExecuted().get() > ZERO) {
                start = System.currentTimeMillis();
                isStartUpdate = true;
            }
            if (mockRedisCallbackService.getExecuted().get() < expectedSize) {
                Thread.sleep(1_000);
            } else {
                duration = System.currentTimeMillis() - start;
                break;
            }
        }

        Assert.assertEquals(expectedSize, mockRedisCallbackService.getExecuted().get());
        logger.info("total build use time, {}", duration);

        mockRedisCallbackService.reset();
        Thread.sleep(5_000);
        Assert.assertEquals(ZERO, mockRedisCallbackService.getExecuted().get());
    }

    private void initData() throws SQLException {
        try {
            int i = 1;
            for (; i < maxTestSize; ) {
                IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(i, 0);
                for (IEntity entity : entities) {
                    masterStorage.build(entity, getEntityClass(entity.entityClassRef().getId()));
                }
                expectedSize += entities.length;
                i += entities.length;
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
