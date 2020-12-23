package com.xforceplus.ultraman.oqsengine.cdc.benchmark;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.CDCAbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.impl.SphinxConsumerToolsTest;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.shaded.org.apache.commons.lang.time.StopWatch;

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
        startMysql();
        startManticore();
        startRedis();
        startCannal();

        entries = new ArrayList<>(size);
        preWarms = new ArrayList<>(1);
        build(preWarms, 1, Long.MAX_VALUE);
        build(entries, 1000, startId);
        cdcMetricsService = new CDCMetricsService();
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", new MockRedisCallbackService());
    }

    @AfterClass
    public static void afterClass() {
        cdcMetricsService.shutdown();
    }

    @Before
    public void before() throws Exception {
        sphinxConsumerService = initAll();
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

        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        CDCMetrics cdcMetrics = sphinxConsumerService.consume(entries, 2, cdcMetricsService);
        stopWatch.stop();

        Assert.assertEquals(size, cdcMetrics.getCdcAckMetrics().getExecuteRows());
        logger.info("end sphinxConsumerBenchmarkTest loops : {}, use timeMs : {} ms",
                cdcMetrics.getCdcAckMetrics().getExecuteRows(), stopWatch.getTime());
    }

    @Test
    public void parseBenchmarkTest() throws InvalidProtocolBufferException {
        //  预热
        parse(preWarms.get(0));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (CanalEntry.Entry entry : entries) {
            parse(entry);
        }
        stopWatch.stop();

        logger.info("end parseBenchmarkTest loops : {}, use timeMs : {} ms", size, stopWatch.getTime());
    }

    private void parse(CanalEntry.Entry entry) throws InvalidProtocolBufferException {
        CanalEntry.RowChange.parseFrom(entry.getStoreValue());
    }

    private static void build(List<CanalEntry.Entry> entries, int size, long startId) {
        for (int i = 0; i < size; i++) {
            long start = startId + i;
            CanalEntry.Entry fRanDom_1 = buildRow(start, true, 1, startId, "0", start,
                    i % SphinxConsumerToolsTest.Prepared.metas.length, 0, 0, 1);
            entries.add(fRanDom_1);
        }
    }
}
