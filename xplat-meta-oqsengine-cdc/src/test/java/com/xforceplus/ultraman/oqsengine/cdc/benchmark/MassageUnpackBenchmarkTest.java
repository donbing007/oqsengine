package com.xforceplus.ultraman.oqsengine.cdc.benchmark;

import static com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools.buildRow;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.AbstractCDCContainer;
import com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * desc :.
 * name : MassageUnpackBenchmarkTest
 *
 * @author : xujia 2020/11/13
 * @since : 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS, ContainerType.MYSQL, ContainerType.MANTICORE, ContainerType.CANNAL})
public class MassageUnpackBenchmarkTest extends AbstractCDCContainer {
    final Logger logger = LoggerFactory.getLogger(MassageUnpackBenchmarkTest.class);
    private static List<CanalEntry.Entry> entries;
    private static List<CanalEntry.Entry> preWarms;

    private static int size = 1000;
    private static long startId = 1;

    private static CDCMetricsService cdcMetricsService;
    private ConsumerService sphinxConsumerService;

    @BeforeClass
    public static void beforeClass() {
        entries = new ArrayList<>(size);
        preWarms = new ArrayList<>(1);
        build(preWarms, 1, Long.MAX_VALUE);
        build(entries, 1000, startId);
        cdcMetricsService = new CDCMetricsService();
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", new MockRedisCallbackService(null));
    }

    @AfterClass
    public static void afterClass() {
        cdcMetricsService.shutdown();
    }

    @Before
    public void before() throws Exception {
        sphinxConsumerService = initAll(false);
    }

    @After
    public void after() throws SQLException {
        clear();
        closeAll();
    }

    @Test
    public void sphinxConsumerBenchmarkTest() throws Exception {
        //  预热
        sphinxConsumerService.consume(preWarms, 1, cdcMetricsService);

        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            CDCMetrics cdcMetrics = sphinxConsumerService.consume(entries, 2, cdcMetricsService);
            long duration = System.currentTimeMillis() - start;

            Assert.assertEquals(size, cdcMetrics.getCdcAckMetrics().getExecuteRows());
            logger.info("end sphinxConsumerBenchmarkTest, loop : {} excuted-Rows : {}, use timeMs : {} ms",
                i, cdcMetrics.getCdcAckMetrics().getExecuteRows(), duration);
        }
    }

    @Test
    public void parseBenchmarkTest() throws InvalidProtocolBufferException {
        //  预热
        parse(preWarms.get(0));

        long start = System.currentTimeMillis();
        for (CanalEntry.Entry entry : entries) {
            parse(entry);
        }
        long duration = System.currentTimeMillis() - start;

        logger.info("end parseBenchmarkTest loops : {}, use timeMs : {} ms", size, duration);
    }

    private void parse(CanalEntry.Entry entry) throws InvalidProtocolBufferException {
        CanalEntry.RowChange.parseFrom(entry.getStoreValue());
    }

    private static void build(List<CanalEntry.Entry> entries, int size, long startId) {
        for (int i = 0; i < size; i++) {
            long start = startId + i;
            CanalEntry.Entry fatherRanDom1 =
                buildRow(start, 1, Long.MAX_VALUE, true, 1, i % CanalEntryTools.Prepared.attrs.length, "false", 0, 1, 1,
                    false);

            entries.add(fatherRanDom1);
        }
    }
}
