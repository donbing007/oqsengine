package com.xforceplus.ultraman.oqsengine.cdc.consumer.process;


import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.context.RunnerContext;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.entry.CanalEntryBuilder;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.repo.DynamicCanalEntryRepo;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
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
    public void executeOneBatchTest() throws Exception {
        cdcConnector.setEntries(CanalEntryBuilder.initAll(expectedDynamic));

        CdcInitialization.getInstance().getBatchProcessor().executeOneBatch(cdcConnector, runnerContext);

        final int expectedExecuteSize = 2;
        final int expectedCommitIdSize = 2;
        final int expectedUnCommitIdSize = 0;

        Assertions.assertEquals(expectedExecuteSize, runnerContext.getCdcMetrics().getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(expectedCommitIdSize, runnerContext.getCdcMetrics().getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(expectedUnCommitIdSize, runnerContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().size());
    }

    @Test
    public void executeEmptyBatchTest() throws Exception {
        CdcInitialization.getInstance().getBatchProcessor().executeOneBatch(cdcConnector, runnerContext);

        Assertions.assertEquals(0, runnerContext.getCdcMetrics().getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(0, runnerContext.getCdcMetrics().getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(0, runnerContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().size());
    }

    @Test
    public void executeOverBatchTest() throws Exception {
        Tuple2<List<CanalEntry.Entry>, CanalEntry.Entry> tuple2 = CanalEntryBuilder.initOverBatch(expectedDynamic);
        cdcConnector.setEntries(tuple2._1());
        CdcInitialization.getInstance().getBatchProcessor().executeOneBatch(cdcConnector, runnerContext);
        final int expectedExecuteSize = 2;
        final int expectedCommitIdSize = 1;
        final int expectedUnCommitIdSize = 1;

        Assertions.assertEquals(expectedExecuteSize, runnerContext.getCdcMetrics().getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(expectedCommitIdSize, runnerContext.getCdcMetrics().getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(expectedUnCommitIdSize, runnerContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().size());

        cdcConnector.setEntries(Collections.singletonList(tuple2._2()));
        CdcInitialization.getInstance().getBatchProcessor().executeOneBatch(cdcConnector, runnerContext);

        Assertions.assertEquals(0, runnerContext.getCdcMetrics().getCdcAckMetrics().getExecuteRows());
        Assertions.assertEquals(1, runnerContext.getCdcMetrics().getCdcAckMetrics().getCommitList().size());
        Assertions.assertEquals(0, runnerContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().size());
    }

}
