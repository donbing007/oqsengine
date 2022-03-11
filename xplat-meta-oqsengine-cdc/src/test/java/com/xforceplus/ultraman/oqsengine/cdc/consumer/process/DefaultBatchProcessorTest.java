package com.xforceplus.ultraman.oqsengine.cdc.consumer.process;


import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.parser.helper.ParseResultCheckHelper;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.service.DefaultConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.context.RunnerContext;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.StaticCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.entry.CanalEntryBuilder;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.repo.DynamicCanalEntryRepo;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.repo.StaticCanalEntryRepo;
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
public class DefaultBatchProcessorTest extends AbstractCdcHelper {

    private MockedCdcConnector cdcConnector;
    private RunnerContext runnerContext;

    @BeforeEach
    public void before() throws Exception {
        super.init(false, null);
        cdcConnector = new MockedCdcConnector();
        runnerContext = new RunnerContext();
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
    public void executeOneBatchTest() throws Exception {
        cdcConnector.setEntries(CanalEntryBuilder.initAll(expectedDynamic, expectedStatic));

        CdcInitialization.getInstance().getBatchProcessor().executeOneBatch(cdcConnector, runnerContext);

        final int expectedDevOpsSize = 2;
        final int expectedExecuteSize = 4;
        final int expectedCommitIdSize = 4;
        final int expectedUnCommitIdSize = 0;

        Assertions.assertEquals(expectedDevOpsSize, runnerContext.getCdcMetrics().getDevOpsMetrics().size());
        Assertions.assertEquals(expectedExecuteSize, runnerContext.getCdcMetrics().getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(expectedCommitIdSize, runnerContext.getCdcMetrics().getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(expectedUnCommitIdSize, runnerContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().size());
    }

    @Test
    public void executeEmptyBatchTest() throws Exception {
        CdcInitialization.getInstance().getBatchProcessor().executeOneBatch(cdcConnector, runnerContext);

        Assertions.assertEquals(0, runnerContext.getCdcMetrics().getDevOpsMetrics().size());
        Assertions.assertEquals(0, runnerContext.getCdcMetrics().getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(0, runnerContext.getCdcMetrics().getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(0, runnerContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().size());
    }

    @Test
    public void executeOverBatchTest() throws Exception {
        cdcConnector.setEntries(CanalEntryBuilder.initExceptLastStatic(expectedDynamic, expectedStatic));
        CdcInitialization.getInstance().getBatchProcessor().executeOneBatch(cdcConnector, runnerContext);
        final int expectedDevOpsSize = 1;
        final int expectedExecuteSize = 3;
        final int expectedCommitIdSize = 3;
        final int expectedUnCommitIdSize = 1;

        Assertions.assertEquals(expectedDevOpsSize, runnerContext.getCdcMetrics().getDevOpsMetrics().size());
        Assertions.assertEquals(expectedExecuteSize, runnerContext.getCdcMetrics().getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(expectedCommitIdSize, runnerContext.getCdcMetrics().getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(expectedUnCommitIdSize, runnerContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().size());

        ParseResult clone =
            ((DefaultConsumerService) CdcInitialization.getInstance().getConsumerService()).printParseResult();

        Assertions.assertEquals(1, clone.getOperationEntries().size());

        Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase> overCase = expectedStatic.get(1);
        OriginalEntity actual = clone.getOperationEntries().get(overCase._1().getId());
        Assertions.assertNotNull(actual);

        ParseResultCheckHelper.staticCheck(overCase, actual, false);

        cdcConnector.setEntries(CanalEntryBuilder.initOverBatchStatic(expectedStatic));
        CdcInitialization.getInstance().getBatchProcessor().executeOneBatch(cdcConnector, runnerContext);

        Assertions.assertEquals(1, runnerContext.getCdcMetrics().getDevOpsMetrics().size());
        Assertions.assertEquals(1, runnerContext.getCdcMetrics().getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(1, runnerContext.getCdcMetrics().getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(0, runnerContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().size());

    }


}
