package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.CDCAbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools.*;

/**
 * desc :
 * name : SphinxConsumerServiceTest
 *
 * @author : xujia
 * date : 2020/11/9
 * @since : 1.8
 */
public class SphinxConsumerServiceTest extends CDCAbstractContainer {
    private ConsumerService sphinxConsumerService;

    private static final Long EXPECTED_PREF = Long.MAX_VALUE - 1;
    private static final Long EXPECTED_CREF = Long.MAX_VALUE - 2;

    private static final Long EXPECTED_DELETED = Long.MAX_VALUE / 2;

    private static final Long RANDOM_INSERT_1 = Long.MAX_VALUE - 3;
    private static final Long RANDOM_INSERT_2 = Long.MAX_VALUE - 4;

    private static final int EXPECTED_ATTR_INDEX_0 = 0;
    private static final int EXPECTED_ATTR_INDEX_1 = 1;
    private static final int EXPECTED_ATTR_INDEX_2 = 2;

    private int expectedSize = 0;

    private CDCMetricsService cdcMetricsService;

    @BeforeClass
    public static void beforeClass() {
        ContainerStarter.startMysql();
        ContainerStarter.startManticore();
        ContainerStarter.startRedis();
        ContainerStarter.startCannal();
    }

    public void before(boolean isMock) throws Exception {
        sphinxConsumerService = initAll(isMock);
        cdcMetricsService = new CDCMetricsService();
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", new MockRedisCallbackService(commitIdStatusService));
    }

    @Test
    public void errorTest() throws Exception {
        before(true);

        List<CanalEntry.Entry> badEntries = new ArrayList<>();

        List<CanalEntry.Entry> goodEntries = new ArrayList<>();

        long commitId = Long.MAX_VALUE - 1;

        badEntries.add(buildRow(RANDOM_INSERT_1, 1, Long.MAX_VALUE, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_1, 1, 1, true));
        goodEntries.add(buildRow(RANDOM_INSERT_1, 1, Long.MAX_VALUE, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_1, 1, 1, false));

        CanalEntry.Entry e2 = buildRow(EXPECTED_CREF, 3, Long.MAX_VALUE - 2, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_2, 1, 1, false);
        badEntries.add(e2);
        goodEntries.add(e2);


        Exception e = null;
        try {
            sphinxConsumerService.consume(badEntries, 1, cdcMetricsService);
        } catch (Exception ex) {
            e = ex;
        }

        Assert.assertNotNull(e);
        Assert.assertEquals("mock error", e.getMessage());

        CdcErrorQueryCondition cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(1L).setType(ErrorType.DATA_INSERT_ERROR.ordinal()).setStatus(FixedStatus.NOT_FIXED.ordinal());
        Collection<CdcErrorTask> cdcErrorTasks = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assert.assertEquals(1, cdcErrorTasks.size());


        List<OriginalEntity> originalEntities = new ArrayList<>();
        Method m = sphinxSyncExecutor.getClass().getDeclaredMethod("prepareForUpdateDelete", new Class[]{List.class, long.class, long.class});
        m.setAccessible(true);

        originalEntities.add((OriginalEntity) m.invoke(sphinxSyncExecutor, new Object[]{columns(goodEntries.get(0)), RANDOM_INSERT_1, commitId}));
        originalEntities.add((OriginalEntity) m.invoke(sphinxSyncExecutor, new Object[]{columns(goodEntries.get(1)), EXPECTED_CREF, commitId}));

        String value = OriginalEntityUtils.toOriginalEntityStr(originalEntities);
        CdcErrorTask cdcErrorTask = cdcErrorTasks.iterator().next();
        int ret = cdcErrorStorage.submitRecover(cdcErrorTask.getSeqNo(), FixedStatus.SUBMIT_FIX_REQ, value);
        Assert.assertEquals(1, ret);

        e = null;

        CDCMetrics cdcMetrics = null;
        try {
            cdcMetrics = sphinxConsumerService.consume(badEntries, 1, cdcMetricsService);
        } catch (Exception ex) {
            e = ex;
        }

        Assert.assertNull(e);
        Assert.assertNotNull(cdcMetrics);

        Assert.assertEquals(1, cdcMetrics.getBatchId());
        Assert.assertEquals(1, cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size());
        Assert.assertTrue(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().contains(commitId));
        Assert.assertEquals(2, cdcMetrics.getCdcAckMetrics().getExecuteRows());

        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(1L).setType(ErrorType.DATA_INSERT_ERROR.ordinal()).setStatus(FixedStatus.FIXED.ordinal());
        cdcErrorTasks = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assert.assertEquals(1, cdcErrorTasks.size());
        cdcErrorTask = cdcErrorTasks.iterator().next();

        originalEntities = OriginalEntityUtils.toOriginalEntity(metaManager, cdcErrorTask.getOperationObject());
        Assert.assertEquals(2, originalEntities.size());
        for (OriginalEntity o : originalEntities) {
            Assert.assertNotNull(o.getEntityClass());
            Assert.assertNotNull(o.getAttributes());
        }
    }

    private List<CanalEntry.Column> columns(CanalEntry.Entry entry) throws InvalidProtocolBufferException {
        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        Assert.assertTrue(null != rowChange.getRowDatasList() && !rowChange.getRowDatasList().isEmpty());
        return rowChange.getRowDatasList().get(0).getAfterColumnsList();
    }

    /*
        存在父子类变更
     */
    @Test
    public void havePrefCrefTest() throws Exception {

        before(false);

        expectedSize = 0;

        List<CanalEntry.Entry> entries = new ArrayList<>();

        CanalEntry.Entry entryTStart = buildTransactionStart();
        entries.add(entryTStart);

        build(entries, Long.MAX_VALUE, false);

        build(entries, Long.MAX_VALUE - 1, false);

        CanalEntry.Entry entryTEnd = buildTransactionEnd();
        entries.add(entryTEnd);


        CDCMetrics cdcMetrics = new CDCMetrics();

        Method m = sphinxConsumerService.getClass().getDeclaredMethod("syncAfterDataFilter", new Class[]{List.class, CDCMetrics.class, CDCMetricsService.class});
        m.setAccessible(true);

        int count = (int) m.invoke(sphinxConsumerService, new Object[]{entries, cdcMetrics, cdcMetricsService});

        Assert.assertEquals(expectedSize, count);
    }


    /*
        测试跨批次的Transaction
        所有的CommitID = Long.MAX_VALUE在一个批次
        CommitID < Long.MAX_VALUE在另一个批次
     */
    @Test
    public void overBatchTest1() throws Exception {
        before(false);

        try {
            expectedSize = 0;

            List<CanalEntry.Entry> entries = new ArrayList<>();

            CanalEntry.Entry entryTStart = buildTransactionStart();
            entries.add(entryTStart);

            build(entries, Long.MAX_VALUE, false);

            CDCMetrics after_1 = sphinxConsumerService.consume(entries, 1, cdcMetricsService);

            Assert.assertEquals(expectedSize, after_1.getCdcAckMetrics().getExecuteRows());

            build(entries, Long.MAX_VALUE - 1, false);

            CanalEntry.Entry entryTEnd = buildTransactionEnd();
            entries.add(entryTEnd);

            cdcMetricsService.getCdcMetrics().setCdcUnCommitMetrics(after_1.getCdcUnCommitMetrics());
            CDCMetrics after_2 = sphinxConsumerService.consume(entries, 2, cdcMetricsService);

            Assert.assertEquals(expectedSize, after_2.getCdcAckMetrics().getExecuteRows());
        } finally {
            closeAll();
        }
    }

    /*
        测试跨批次的Transaction
        所有的CommitID = Long.MAX_VALUE在一个批次, 同时包含2条CommitID < Long.MAX_VALUE数据
        其他CommitID < Long.MAX_VALUE在另一个批次
     */
    @Test
    public void overBatchTest2() throws Exception {
        before(false);

        try {
            expectedSize = 0;

            List<CanalEntry.Entry> entries = new ArrayList<>();

            CanalEntry.Entry entryTStart = buildTransactionStart();
            entries.add(entryTStart);

            build(entries, Long.MAX_VALUE, false);

            //  random 1
            long commitId = Long.MAX_VALUE - 1;
            CanalEntry.Entry fRanDom_1 = buildRow(RANDOM_INSERT_1, 1, Long.MAX_VALUE, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_1, 1, 1, false);
            entries.add(fRanDom_1);

            addExpectCount(commitId);

            //  build child
            CanalEntry.Entry cEntryUnCommit = buildRow(EXPECTED_CREF, 3, Long.MAX_VALUE - 2, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_2, 1, 1, false);
            entries.add(cEntryUnCommit);

            addExpectCount(commitId);

            CDCMetrics after_1 = sphinxConsumerService.consume(entries, 1, cdcMetricsService);

            Assert.assertEquals(expectedSize, after_1.getCdcAckMetrics().getExecuteRows());

            expectedSize = 0;
            entries.clear();
        /*
            这里将子类设定为比父类先到，在批次1中，父类在批次二中，模拟了真实情况的不确定性
         */
            //  build father
            CanalEntry.Entry fEntryUnCommit = buildRow(EXPECTED_PREF, 2, Long.MAX_VALUE - 1, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_0, 1, 1, false);


            entries.add(fEntryUnCommit);

            addExpectCount(commitId);

            //  build delete
            CanalEntry.Entry cDeleted = buildRow(EXPECTED_DELETED, 1, Long.MAX_VALUE, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_0, 1, 1, false);

            entries.add(cDeleted);

            addExpectCount(commitId);

            //  random 2
            CanalEntry.Entry fRanDom_2 = buildRow(RANDOM_INSERT_2, 1, Long.MAX_VALUE, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_0, 1, 1, false);
            entries.add(fRanDom_2);

            addExpectCount(commitId);


            CanalEntry.Entry entryTEnd = buildTransactionEnd();
            entries.add(entryTEnd);

            cdcMetricsService.getCdcMetrics().setCdcUnCommitMetrics(after_1.getCdcUnCommitMetrics());
            CDCMetrics after_2 = sphinxConsumerService.consume(entries, 2, cdcMetricsService);

            Assert.assertEquals(expectedSize, after_2.getCdcAckMetrics().getExecuteRows());
        } finally {
            closeAll();
        }
    }

    private void build(List<CanalEntry.Entry> entries, long commitId, boolean buildError) {
        //  random 1
        CanalEntry.Entry fRanDom_1 = buildRow(RANDOM_INSERT_1, 2, Long.MAX_VALUE - 1, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_1, 1, 1, buildError);

        entries.add(fRanDom_1);

        addExpectCount(commitId);

        //  build father
        CanalEntry.Entry fEntryUnCommit = buildRow(EXPECTED_PREF, 2, Long.MAX_VALUE - 1, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_0, 1, 1, buildError);
        entries.add(fEntryUnCommit);

        addExpectCount(commitId);

        //  build child
        CanalEntry.Entry cEntryUnCommit = buildRow(EXPECTED_CREF, 3, Long.MAX_VALUE - 2, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_2, 1, 1, buildError);
        entries.add(cEntryUnCommit);

        addExpectCount(commitId);

        //  build delete
        CanalEntry.Entry cDeleted = buildRow(EXPECTED_DELETED, 3, Long.MAX_VALUE - 2, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_2, 1, 1, buildError);
        entries.add(cDeleted);

        addExpectCount(commitId);

        //  random 2
        CanalEntry.Entry fRanDom_2 = buildRow(RANDOM_INSERT_2, 3, Long.MAX_VALUE - 2, true, 1, commitId, "false", EXPECTED_ATTR_INDEX_2, 1, 1, buildError);
        entries.add(fRanDom_2);

        addExpectCount(commitId);
    }

    private void addExpectCount(Long commitId) {
        if (commitId != Long.MAX_VALUE) {
            expectedSize ++;
        }
    }

}
