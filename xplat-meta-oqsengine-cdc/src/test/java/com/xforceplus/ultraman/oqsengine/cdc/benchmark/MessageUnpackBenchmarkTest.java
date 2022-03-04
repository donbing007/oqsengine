package com.xforceplus.ultraman.oqsengine.cdc.benchmark;

import static com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools.buildRow;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.AbstractCDCTestHelper;
import com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :.
 * name : MessageUnpackBenchmarkTest
 *
 * @author : xujia 2020/11/13
 * @since : 1.8
 */
public class MessageUnpackBenchmarkTest extends AbstractCDCTestHelper {
    final Logger logger = LoggerFactory.getLogger(MessageUnpackBenchmarkTest.class);
    private static List<CanalEntry.Entry> entries;
    private static List<CanalEntry.Entry> preWarms;

    private static int size = 1000;
    private static long startId = 1;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        super.init(true);
        entries = new ArrayList<>();
        preWarms = new ArrayList<>();
        build(preWarms, 1, Long.MAX_VALUE);
        build(entries, 1000, startId);
    }

    @AfterEach
    public void after() throws Exception {
        super.clear(true);
    }

    @AfterAll
    public static void afterAll() {
        InitializationHelper.destroy();
    }

    @Test
    public void sphinxConsumerBenchmarkTest() throws Exception {
        ConsumerService consumerService = CdcInitialization.getInstance().getConsumerService();
        //  预热
        consumerService.consume(preWarms, 1, cdcMetricsService);

        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            CDCMetrics cdcMetrics = consumerService.consume(entries, 2, cdcMetricsService);
            long duration = System.currentTimeMillis() - start;

            Assertions.assertEquals(size, cdcMetrics.getCdcAckMetrics().getExecuteRows());
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
