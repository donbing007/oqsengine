package com.xforceplus.ultraman.oqsengine.cdc.benchmark;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.impl.SphinxConsumerToolsTest;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCUnCommitMetrics;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MassageUnpackBenchmarkTest extends AbstractContainer {
    final Logger logger = LoggerFactory.getLogger(MassageUnpackBenchmarkTest.class);
    private static List<CanalEntry.Entry> entries;
    private static List<CanalEntry.Entry> preWarms;

    private static int size = 10000;
    private static long startId = 1;
    @BeforeClass
    public static void beforeClass() {

        entries = new ArrayList<>(size);
        preWarms = new ArrayList<>(1);
        build(preWarms, 1, Long.MAX_VALUE);
        build(entries, 10000, startId);
    }

    @Test
    public void sphinxConsumerBenchmarkTest() throws SQLException, InterruptedException {
        ConsumerService sphinxConsumerService = initConsumerService();
        //  预热
        sphinxConsumerService.consume(preWarms, 1, new CDCUnCommitMetrics());

        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        sphinxConsumerService.consume(entries, 2, new CDCUnCommitMetrics());

        stopWatch.stop();

        logger.info("end sphinxConsumerBenchmarkTest loops : {}, use timeMs : {} ms", size, stopWatch.getTime());
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
            CanalEntry.Entry fRanDom_1 = buildRow(start, true, 1, start, "0", start,
                    i % SphinxConsumerToolsTest.Prepared.metas.length, 0, 0);
            entries.add(fRanDom_1);
        }
    }
}
