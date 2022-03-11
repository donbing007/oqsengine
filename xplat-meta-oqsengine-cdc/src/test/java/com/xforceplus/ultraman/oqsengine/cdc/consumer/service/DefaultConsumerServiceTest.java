package com.xforceplus.ultraman.oqsengine.cdc.consumer.service;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.parser.helper.ParseResultCheckHelper;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.StaticCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.entry.CanalEntryBuilder;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.repo.DynamicCanalEntryRepo;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.repo.StaticCanalEntryRepo;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import io.vavr.Tuple2;
import java.util.Arrays;
import java.util.List;
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

    private static final List<DynamicCanalEntryCase> expectedDynamic =
        Arrays.asList(DynamicCanalEntryRepo.CASE_NORMAL_2, DynamicCanalEntryRepo.CASE_MAINTAIN);

    private static final List<Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase>> expectedStatic =
        Arrays.asList(StaticCanalEntryRepo.CASE_STATIC_ALL_IN_ONE, StaticCanalEntryRepo.CASE_STATIC_MAINTAIN);

    @Test
    public void consumeTest() throws Exception {
        final long expectedBatchId = 2001;
        final int expectedDevOpsSize = 2;
        final int expectedExecuteSize = 4;
        final int expectedCommitIdSize = 4;
        final int expectedUnCommitIdSize = 0;

        CDCMetrics cdcMetrics =
            CdcInitialization.getInstance().getConsumerService().consumeOneBatch(CanalEntryBuilder.initAll(expectedDynamic, expectedStatic), expectedBatchId, new CDCMetrics());

        Assertions.assertNotNull(cdcMetrics);
        Assertions.assertEquals(expectedBatchId, cdcMetrics.getBatchId());
        Assertions.assertEquals(expectedDevOpsSize, cdcMetrics.getDevOpsMetrics().size());
        Assertions.assertEquals(expectedExecuteSize, cdcMetrics.getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(expectedCommitIdSize, cdcMetrics.getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(expectedUnCommitIdSize, cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size());
    }

    @Test
    public void consumeOverBatchTest() throws Exception {
        long expectedBatchId = 2002;
        final int expectedDevOpsSize = 1;
        final int expectedExecuteSize = 3;
        final int expectedCommitIdSize = 3;
        final int expectedUnCommitIdSize = 1;

        CDCMetrics cdcMetrics =
            CdcInitialization.getInstance().getConsumerService().consumeOneBatch(CanalEntryBuilder.initExceptLastStatic(expectedDynamic, expectedStatic), expectedBatchId, new CDCMetrics());

        Assertions.assertNotNull(cdcMetrics);
        Assertions.assertEquals(expectedBatchId, cdcMetrics.getBatchId());
        Assertions.assertEquals(expectedDevOpsSize, cdcMetrics.getDevOpsMetrics().size());
        Assertions.assertEquals(expectedExecuteSize, cdcMetrics.getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(expectedCommitIdSize, cdcMetrics.getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(expectedUnCommitIdSize, cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size());

        ParseResult clone =
            ((DefaultConsumerService) CdcInitialization.getInstance().getConsumerService()).printParseResult();

        Assertions.assertEquals(1, clone.getOperationEntries().size());

        Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase> overCase = expectedStatic.get(1);
        OriginalEntity actual = clone.getOperationEntries().get(overCase._1().getId());
        Assertions.assertNotNull(actual);

        ParseResultCheckHelper.staticCheck(overCase, actual, false);

        expectedBatchId = expectedBatchId + 1;
        CDCMetrics cdcMetricsOver =
            CdcInitialization.getInstance().getConsumerService().consumeOneBatch(CanalEntryBuilder.initOverBatchStatic(expectedStatic), expectedBatchId, cdcMetrics);

        Assertions.assertNotNull(cdcMetricsOver);
        Assertions.assertEquals(expectedBatchId, cdcMetricsOver.getBatchId());
        Assertions.assertEquals(1, cdcMetricsOver.getDevOpsMetrics().size());
        Assertions.assertEquals(1, cdcMetricsOver.getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(1, cdcMetricsOver.getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(0, cdcMetricsOver.getCdcUnCommitMetrics().getUnCommitIds().size());
    }



}
