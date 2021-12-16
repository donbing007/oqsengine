package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import static com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools.buildRow;
import static com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools.buildTransactionEnd;
import static com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools.buildTransactionStart;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.AbstractCDCTestHelper;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.CdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * desc :.
 * name : SphinxConsumerServiceTest
 *
 * @author : xujia 2020/11/9
 * @since : 1.8
 */
public class SphinxConsumerServiceTest extends AbstractCDCTestHelper {

    private static final Long EXPECTED_PREF = Long.MAX_VALUE - 1;
    private static final Long EXPECTED_CREF = Long.MAX_VALUE - 2;

    private static final Long EXPECTED_DELETED = Long.MAX_VALUE / 2;

    private static final Long RANDOM_INSERT_1 = Long.MAX_VALUE - 3;
    private static final Long RANDOM_INSERT_2 = Long.MAX_VALUE - 4;

    private static final int EXPECTED_ATTR_INDEX_0 = 0;
    private static final int EXPECTED_ATTR_INDEX_1 = 1;
    private static final int EXPECTED_ATTR_INDEX_2 = 2;

    private int expectedSize = 0;

    @BeforeEach
    public void before() throws Exception {
        super.init(false);
    }

    @AfterEach
    public void after() throws Exception {
        super.destroy(false);
    }


    @Test
    public void errorTest() throws Exception {
        doBefore(true);

        List<CanalEntry.Entry> badEntries = new ArrayList<>();

        List<CanalEntry.Entry> goodEntries = new ArrayList<>();

        long commitId = Long.MAX_VALUE - 1;

        badEntries.add(
            buildRow(RANDOM_INSERT_1, 1, Long.MAX_VALUE, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_1, 1, 1,
                true));
        goodEntries.add(
            buildRow(RANDOM_INSERT_1, 1, Long.MAX_VALUE, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_1, 1, 1,
                false));

        CanalEntry.Entry e2 =
            buildRow(EXPECTED_CREF, 3, Long.MAX_VALUE - 2, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_2, 1, 1,
                false);
        badEntries.add(e2);
        goodEntries.add(e2);


        Exception e = null;
        ConsumerService consumerService = CdcInitialization.getInstance().getConsumerService();

        try {
            consumerService.consume(badEntries, 1, cdcMetricsService);
        } catch (Exception ex) {
            e = ex;
        }

        Assertions.assertNotNull(e);
        Assertions.assertEquals("mock error", e.getMessage());

        CdcErrorQueryCondition cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(1L).setType(ErrorType.DATA_INSERT_ERROR.getType())
            .setStatus(FixedStatus.NOT_FIXED.getStatus());
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();
        Collection<CdcErrorTask> cdcErrorTasks = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assertions.assertEquals(1, cdcErrorTasks.size());


        List<OriginalEntity> originalEntities = new ArrayList<>();

        SphinxSyncExecutor sphinxSyncExecutor = CdcInitialization.getInstance().getSphinxSyncExecutor();

        Method m = sphinxSyncExecutor.getClass()
            .getDeclaredMethod("prepareForUpdateDelete", new Class[] {List.class, long.class, long.class, Map.class});
        m.setAccessible(true);

        originalEntities.add((OriginalEntity) m
            .invoke(sphinxSyncExecutor, new Object[] {columns(goodEntries.get(0)), RANDOM_INSERT_1, commitId, new HashMap<>()}));
        originalEntities.add((OriginalEntity) m
            .invoke(sphinxSyncExecutor, new Object[] {columns(goodEntries.get(1)), EXPECTED_CREF, commitId, new HashMap<>()}));

        String value = OriginalEntityUtils.toOriginalEntityStr(originalEntities);
        CdcErrorTask cdcErrorTask = cdcErrorTasks.iterator().next();
        int ret = cdcErrorStorage.submitRecover(cdcErrorTask.getSeqNo(), FixedStatus.SUBMIT_FIX_REQ, value);
        Assertions.assertEquals(1, ret);

        e = null;

        CDCMetrics cdcMetrics = null;
        try {
            cdcMetrics = consumerService.consume(badEntries, 1, cdcMetricsService);
        } catch (Exception ex) {
            e = ex;
        }

        Assertions.assertNull(e);
        Assertions.assertNotNull(cdcMetrics);

        Assertions.assertEquals(1, cdcMetrics.getBatchId());
        Assertions.assertEquals(1, cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size());
        Assertions.assertTrue(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().contains(commitId));
        Assertions.assertEquals(2, cdcMetrics.getCdcAckMetrics().getExecuteRows());

        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(1L).setType(ErrorType.DATA_INSERT_ERROR.getType())
            .setStatus(FixedStatus.FIXED.getStatus());
        cdcErrorTasks = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assertions.assertEquals(1, cdcErrorTasks.size());
        cdcErrorTask = cdcErrorTasks.iterator().next();

        originalEntities = OriginalEntityUtils
            .toOriginalEntity(MetaInitialization.getInstance().getMetaManager(), cdcErrorTask.getOperationObject());
        Assertions.assertEquals(2, originalEntities.size());
        for (OriginalEntity o : originalEntities) {
            Assertions.assertNotNull(o.getEntityClass());
            Assertions.assertNotNull(o.getAttributes());
        }
    }

    private List<CanalEntry.Column> columns(CanalEntry.Entry entry) throws InvalidProtocolBufferException {
        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        Assertions.assertFalse(rowChange.getRowDatasList().isEmpty());
        return rowChange.getRowDatasList().get(0).getAfterColumnsList();
    }

    /*
        存在父子类变更
     */
    @Test
    public void havePrefCrefTest() throws Exception {

        doBefore(false);

        expectedSize = 0;

        List<CanalEntry.Entry> entries = new ArrayList<>();

        CanalEntry.Entry entryTStart = buildTransactionStart();
        entries.add(entryTStart);

        build(entries, Long.MAX_VALUE, false);

        build(entries, Long.MAX_VALUE - 1, false);

        CanalEntry.Entry entryTEnd = buildTransactionEnd();
        entries.add(entryTEnd);


        CDCMetrics cdcMetrics = new CDCMetrics();
        ConsumerService consumerService = CdcInitialization.getInstance().getConsumerService();

        Method m = consumerService.getClass().getDeclaredMethod("syncAfterDataFilter",
            new Class[] {List.class, CDCMetrics.class, CDCMetricsService.class});
        m.setAccessible(true);

        int count = (int) m.invoke(consumerService, new Object[] {entries, cdcMetrics, cdcMetricsService});

        Assertions.assertEquals(expectedSize, count);
    }


    /*
        测试跨批次的Transaction
        所有的CommitID = Long.MAX_VALUE在一个批次
        CommitID < Long.MAX_VALUE在另一个批次
     */
    @Test
    public void overBatchTest1() throws Exception {
        doBefore(false);

        expectedSize = 0;

        List<CanalEntry.Entry> entries = new ArrayList<>();

        CanalEntry.Entry entryTStart = buildTransactionStart();
        entries.add(entryTStart);

        build(entries, Long.MAX_VALUE, false);

        ConsumerService consumerService = CdcInitialization.getInstance().getConsumerService();

        CDCMetrics after1 = consumerService.consume(entries, 1, cdcMetricsService);

        Assertions.assertEquals(expectedSize, after1.getCdcAckMetrics().getExecuteRows());

        build(entries, Long.MAX_VALUE - 1, false);

        CanalEntry.Entry entryTEnd = buildTransactionEnd();
        entries.add(entryTEnd);

        cdcMetricsService.getCdcMetrics().setCdcUnCommitMetrics(after1.getCdcUnCommitMetrics());
        CDCMetrics after2 = consumerService.consume(entries, 2, cdcMetricsService);

        Assertions.assertEquals(expectedSize, after2.getCdcAckMetrics().getExecuteRows());

    }

    /*
        测试跨批次的Transaction
        所有的CommitID = Long.MAX_VALUE在一个批次, 同时包含2条CommitID < Long.MAX_VALUE数据
        其他CommitID < Long.MAX_VALUE在另一个批次
     */
    @Test
    public void overBatchTest2() throws Exception {
        doBefore(false);

        expectedSize = 0;

        List<CanalEntry.Entry> entries = new ArrayList<>();

        CanalEntry.Entry entryTStart = buildTransactionStart();
        entries.add(entryTStart);

        build(entries, Long.MAX_VALUE, false);

        //  random 1
        long commitId = Long.MAX_VALUE - 1;
        CanalEntry.Entry fatherRanDom1 =
            buildRow(RANDOM_INSERT_1, 1, Long.MAX_VALUE, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_1, 1, 1,
                false);
        entries.add(fatherRanDom1);

        addExpectCount(commitId);

        //  build child
        CanalEntry.Entry canalEntryUnCommit =
            buildRow(
                EXPECTED_CREF,
                3,
                Long.MAX_VALUE - 2,
                true,
                1, commitId,
                "false",
                EXPECTED_ATTR_INDEX_2,
                1,
                1,
                false);
        entries.add(canalEntryUnCommit);

        addExpectCount(commitId);

        ConsumerService consumerService = CdcInitialization.getInstance().getConsumerService();

        CDCMetrics after1 = consumerService.consume(entries, 1, cdcMetricsService);

        Assertions.assertEquals(expectedSize, after1.getCdcAckMetrics().getExecuteRows());

        expectedSize = 0;
        entries.clear();
        /*
           这里将子类设定为比父类先到，在批次1中，父类在批次二中，模拟了真实情况的不确定性
         */
        //  build father
        CanalEntry.Entry fatherEntryUnCommit =
            buildRow(
                EXPECTED_PREF,
                2,
                Long.MAX_VALUE - 1,
                true,
                1,
                commitId,
                "false",
                EXPECTED_ATTR_INDEX_0,
                1,
                1,
                false);

        entries.add(fatherEntryUnCommit);

        addExpectCount(commitId);

        //  build delete
        CanalEntry.Entry cdcDeleted =
            buildRow(EXPECTED_DELETED, 1, Long.MAX_VALUE, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_0, 1, 1,
                false);

        entries.add(cdcDeleted);

        addExpectCount(commitId);

        //  random 2
        CanalEntry.Entry canalRanDom2 =
            buildRow(RANDOM_INSERT_2, 1, Long.MAX_VALUE, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_0, 1, 1,
                false);
        entries.add(canalRanDom2);

        addExpectCount(commitId);


        CanalEntry.Entry entryTEnd = buildTransactionEnd();
        entries.add(entryTEnd);

        cdcMetricsService.getCdcMetrics().setCdcUnCommitMetrics(after1.getCdcUnCommitMetrics());
        CDCMetrics after2 = consumerService.consume(entries, 2, cdcMetricsService);

        Assertions.assertEquals(expectedSize, after2.getCdcAckMetrics().getExecuteRows());
    }

    private void build(List<CanalEntry.Entry> entries, long commitId, boolean buildError) {
        //  random 1
        CanalEntry.Entry canalRanDom1 =
            buildRow(RANDOM_INSERT_1, 2, Long.MAX_VALUE - 1, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_1, 1, 1,
                buildError);

        entries.add(canalRanDom1);

        addExpectCount(commitId);

        //  build father
        CanalEntry.Entry fatherEntryUnCommit =
            buildRow(EXPECTED_PREF, 2, Long.MAX_VALUE - 1, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_0, 1, 1,
                buildError);
        entries.add(fatherEntryUnCommit);

        addExpectCount(commitId);

        //  build child
        CanalEntry.Entry childEntryUnCommit =
            buildRow(EXPECTED_CREF, 3, Long.MAX_VALUE - 2, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_2, 1, 1,
                buildError);
        entries.add(childEntryUnCommit);

        addExpectCount(commitId);

        //  build delete
        CanalEntry.Entry childDeleted =
            buildRow(EXPECTED_DELETED, 3, Long.MAX_VALUE - 2, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_2, 1, 1,
                buildError);
        entries.add(childDeleted);

        addExpectCount(commitId);

        //  random 2
        CanalEntry.Entry fatherRanDom2 =
            buildRow(RANDOM_INSERT_2, 3, Long.MAX_VALUE - 2, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_2, 1, 1,
                buildError);
        entries.add(fatherRanDom2);

        addExpectCount(commitId);
    }

    private void addExpectCount(Long commitId) {
        if (commitId != Long.MAX_VALUE) {
            expectedSize++;
        }
    }

    private void doBefore(boolean mock) throws Exception {
        cdcMetricsService = new CDCMetricsService();
        if (mock) {
            CdcInitialization.getInstance().useMock();
        } else {
            CdcInitialization.getInstance().useReal();
        }
        ReflectionTestUtils
            .setField(cdcMetricsService, "cdcMetricsCallback",
                new MockRedisCallbackService(StorageInitialization.getInstance()
                    .getCommitIdStatusService()));
    }

}
