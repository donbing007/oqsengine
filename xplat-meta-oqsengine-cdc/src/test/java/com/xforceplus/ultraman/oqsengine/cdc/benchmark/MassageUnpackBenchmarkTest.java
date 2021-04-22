package com.xforceplus.ultraman.oqsengine.cdc.benchmark;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.CDCAbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools.buildRow;

/**
 * desc :
 * name : MassageUnpackBenchmarkTest
 *
 * @author : xujia
 * date : 2020/11/13
 * @since : 1.8
 */
public class MassageUnpackBenchmarkTest extends CDCAbstractContainer {
    final Logger logger = LoggerFactory.getLogger(MassageUnpackBenchmarkTest.class);
    private static List<CanalEntry.Entry> entries;
    private static List<CanalEntry.Entry> preWarms;

    private static int size = 1000;
    private static long startId = 1;

    private static CDCMetricsService cdcMetricsService;
    private ConsumerService sphinxConsumerService;

    @BeforeClass
    public static void beforeClass() {
        ContainerStarter.startMysql();
        ContainerStarter.startManticore();
        ContainerStarter.startRedis();
        ContainerStarter.startCannal();

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

        ContainerStarter.reset();
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

        long start = System.currentTimeMillis();
        CDCMetrics cdcMetrics = sphinxConsumerService.consume(entries, 2, cdcMetricsService);
        long duration = System.currentTimeMillis() - start;

        Assert.assertEquals(size, cdcMetrics.getCdcAckMetrics().getExecuteRows());
        logger.info("end sphinxConsumerBenchmarkTest loops : {}, use timeMs : {} ms",
                cdcMetrics.getCdcAckMetrics().getExecuteRows(), duration);
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
            CanalEntry.Entry fRanDom_1 =
                    buildRow(start, 1, Long.MAX_VALUE, true, 1, i % CanalEntryTools.Prepared.attrs.length, "false", 0, 1, 1, false);

            entries.add(fRanDom_1);
        }
    }
}
