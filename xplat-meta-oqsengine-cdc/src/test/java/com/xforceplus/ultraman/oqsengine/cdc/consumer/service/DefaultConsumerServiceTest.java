package com.xforceplus.ultraman.oqsengine.cdc.consumer.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.entry.CanalEntryBuilder;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.repo.DynamicCanalEntryRepo;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import io.vavr.Tuple2;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class DefaultConsumerServiceTest extends AbstractCdcHelper {

    @BeforeEach
    public void before() throws Exception {
        super.init(false, null);
    }

    @AfterEach
    public void after() throws Exception {
        super.clear(false);
    }

    @AfterAll
    public static void afterAll() {
        try {
            InitializationHelper.destroy();
        } catch (Exception e) {

        }
    }

    private static final List<DynamicCanalEntryCase> expectedDynamic =
        Arrays.asList(DynamicCanalEntryRepo.CASE_NORMAL_2, DynamicCanalEntryRepo.CASE_MAINTAIN);

    @Test
    public void consumeTest() throws Exception {
        final long expectedBatchId = 2001;
        final int expectedExecuteSize = 2;
        final int expectedCommitIdSize = 1;     //  维护ID不在计入统计.
        final int expectedUnCommitIdSize = 0;   //

        CDCMetrics cdcMetrics =
            CdcInitialization.getInstance().getConsumerService().consumeOneBatch(CanalEntryBuilder.initAll(expectedDynamic), expectedBatchId, new CDCMetrics());

        Assertions.assertNotNull(cdcMetrics);
        Assertions.assertEquals(expectedBatchId, cdcMetrics.getBatchId());
        Assertions.assertEquals(expectedExecuteSize, cdcMetrics.getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(expectedCommitIdSize, cdcMetrics.getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(expectedUnCommitIdSize, cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size());
    }

    @Test
    public void consumeOverBatchTest() throws Exception {
        long expectedBatchId = 2002;
        final int expectedDevOpsSize = 1;
        final int expectedExecuteSize = 2;
        final int expectedCommitIdSize = 1;
        final int expectedUnCommitIdSize = 0;   //  维护ID不在加入统计

        Tuple2<List<CanalEntry.Entry>, CanalEntry.Entry> tuple2 = CanalEntryBuilder.initOverBatch(expectedDynamic);

        CDCMetrics cdcMetrics =
            CdcInitialization.getInstance().getConsumerService().consumeOneBatch(tuple2._1(), expectedBatchId, new CDCMetrics());

        Assertions.assertNotNull(cdcMetrics);
        Assertions.assertEquals(expectedBatchId, cdcMetrics.getBatchId());
        Assertions.assertEquals(expectedExecuteSize, cdcMetrics.getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(expectedCommitIdSize, cdcMetrics.getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(expectedUnCommitIdSize, cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size());


        expectedBatchId = expectedBatchId + 1;
        CDCMetrics cdcMetricsOver =
            CdcInitialization.getInstance().getConsumerService().consumeOneBatch(Collections.singletonList(tuple2._2()), expectedBatchId, cdcMetrics);

        Assertions.assertNotNull(cdcMetricsOver);
        Assertions.assertEquals(expectedBatchId, cdcMetricsOver.getBatchId());
        Assertions.assertEquals(0, cdcMetricsOver.getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(0, cdcMetricsOver.getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(0, cdcMetricsOver.getCdcUnCommitMetrics().getUnCommitIds().size());
    }

}
