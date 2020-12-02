package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
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
public class SphinxConsumerServiceTest extends AbstractContainer {
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

    @Before
    public void before() throws Exception {
        sphinxConsumerService = initAll();
    }

    @After
    public void after() throws Exception {
        closeAll();
    }

    /*
        存在父子类变更
     */
    @Test
    public void havePrefCrefTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        expectedSize = 0;

        List<CanalEntry.Entry> entries = new ArrayList<>();

        CanalEntry.Entry entryTStart = buildTransactionStart();
        entries.add(entryTStart);

        build(entries, Long.MAX_VALUE);

        build(entries, Long.MAX_VALUE - 1);

        CanalEntry.Entry entryTEnd = buildTransactionEnd();
        entries.add(entryTEnd);


        CDCMetrics cdcMetrics = new CDCMetrics();

        Method m = sphinxConsumerService.getClass().getDeclaredMethod("syncAfterDataFilter", new Class[]{List.class, CDCMetrics.class});
        m.setAccessible(true);

        int count = (int) m.invoke(sphinxConsumerService, new Object[]{entries, cdcMetrics});

        Assert.assertEquals(expectedSize, count);
    }


    /*
        测试跨批次的Transaction
        所有的CommitID = Long.MAX_VALUE在一个批次
        CommitID < Long.MAX_VALUE在另一个批次
     */
    @Test
    public void overBatchTest1() throws SQLException {
        expectedSize = 0;

        List<CanalEntry.Entry> entries = new ArrayList<>();

        CanalEntry.Entry entryTStart = buildTransactionStart();
        entries.add(entryTStart);

        build(entries, Long.MAX_VALUE);

        CDCMetricsService cdcMetricsService = new CDCMetricsService();
        CDCMetrics after_1 = sphinxConsumerService.consume(entries, 1, cdcMetricsService);

        Assert.assertEquals(expectedSize, after_1.getCdcAckMetrics().getExecuteRows());

        build(entries, Long.MAX_VALUE - 1);

        CanalEntry.Entry entryTEnd = buildTransactionEnd();
        entries.add(entryTEnd);

        cdcMetricsService = new CDCMetricsService();
        cdcMetricsService.getCdcMetrics().setCdcUnCommitMetrics(after_1.getCdcUnCommitMetrics());
        CDCMetrics after_2 = sphinxConsumerService.consume(entries, 2, cdcMetricsService);

        Assert.assertEquals(expectedSize, after_2.getCdcAckMetrics().getExecuteRows());
    }

    /*
        测试跨批次的Transaction
        所有的CommitID = Long.MAX_VALUE在一个批次, 同时包含2条CommitID < Long.MAX_VALUE数据
        其他CommitID < Long.MAX_VALUE在另一个批次
     */
    @Test
    public void overBatchTest2() throws SQLException {
        expectedSize = 0;

        List<CanalEntry.Entry> entries = new ArrayList<>();

        CanalEntry.Entry entryTStart = buildTransactionStart();
        entries.add(entryTStart);

        build(entries, Long.MAX_VALUE);

        //  random 1
        long commitId = Long.MAX_VALUE - 1;
        CanalEntry.Entry fRanDom_1 = buildRow(RANDOM_INSERT_1, true, 1, commitId, "false", RANDOM_INSERT_1, EXPECTED_ATTR_INDEX_2, 0, 0, 1);
        entries.add(fRanDom_1);

        addExpectCount(commitId);

        //  build child
        CanalEntry.Entry cEntryUnCommit = buildRow(EXPECTED_CREF, true, 1, commitId, "false", EXPECTED_CREF, EXPECTED_ATTR_INDEX_1, EXPECTED_PREF, 0, 1);
        entries.add(cEntryUnCommit);

        addExpectCount(commitId);

        CDCMetricsService cdcMetricsService = new CDCMetricsService();
        CDCMetrics after_1 = sphinxConsumerService.consume(entries, 1, cdcMetricsService);

        Assert.assertEquals(expectedSize, after_1.getCdcAckMetrics().getExecuteRows());

        expectedSize = 0;
        entries.clear();
        /*
            这里将子类设定为比父类先到，在批次1中，父类在批次二中，模拟了真实情况的不确定性
         */
        //  build father
        CanalEntry.Entry fEntryUnCommit = buildRow(EXPECTED_PREF, true, 1, commitId, "false", EXPECTED_PREF, EXPECTED_ATTR_INDEX_2, 0, EXPECTED_CREF, 1);
        entries.add(fEntryUnCommit);

        addExpectCount(commitId);

        //  build delete
        CanalEntry.Entry cDeleted = buildRow(EXPECTED_DELETED, true, 1, commitId, "true", EXPECTED_DELETED, EXPECTED_ATTR_INDEX_0, 0, 0, 1);
        entries.add(cDeleted);

        addExpectCount(commitId);

        //  random 2
        CanalEntry.Entry fRanDom_2 = buildRow(RANDOM_INSERT_2, true, 1, commitId, "false", RANDOM_INSERT_2, EXPECTED_ATTR_INDEX_1, 0, 0,1);
        entries.add(fRanDom_2);

        addExpectCount(commitId);


        CanalEntry.Entry entryTEnd = buildTransactionEnd();
        entries.add(entryTEnd);

        cdcMetricsService = new CDCMetricsService();
        cdcMetricsService.getCdcMetrics().setCdcUnCommitMetrics(after_1.getCdcUnCommitMetrics());
        CDCMetrics after_2 = sphinxConsumerService.consume(entries, 2, cdcMetricsService);

        Assert.assertEquals(expectedSize, after_2.getCdcAckMetrics().getExecuteRows());
    }

    private void build(List<CanalEntry.Entry> entries, long commitId) {
        //  random 1
        CanalEntry.Entry fRanDom_1 = buildRow(RANDOM_INSERT_1, true, 1, commitId, "false", RANDOM_INSERT_1, EXPECTED_ATTR_INDEX_2, 0, 0, 1);
        entries.add(fRanDom_1);

        addExpectCount(commitId);

        //  build father
        CanalEntry.Entry fEntryUnCommit = buildRow(EXPECTED_PREF, true, 1, commitId, "false", EXPECTED_PREF, EXPECTED_ATTR_INDEX_2, 0, EXPECTED_CREF, 1);
        entries.add(fEntryUnCommit);

        addExpectCount(commitId);

        //  build child
        CanalEntry.Entry cEntryUnCommit = buildRow(EXPECTED_CREF, true, 1, commitId, "false", EXPECTED_CREF, EXPECTED_ATTR_INDEX_1, EXPECTED_PREF, 0, 1);
        entries.add(cEntryUnCommit);

        addExpectCount(commitId);

        //  build delete
        CanalEntry.Entry cDeleted = buildRow(EXPECTED_DELETED, true, 1, commitId, "true", EXPECTED_DELETED, EXPECTED_ATTR_INDEX_0, 0, 0, 1);
        entries.add(cDeleted);

        addExpectCount(commitId);

        //  random 2
        CanalEntry.Entry fRanDom_2 = buildRow(RANDOM_INSERT_2, true, 1, commitId, "false", RANDOM_INSERT_2, EXPECTED_ATTR_INDEX_1, 0, 0, 1);
        entries.add(fRanDom_2);

        addExpectCount(commitId);
    }

    private void addExpectCount(Long commitId) {
        if (commitId != Long.MAX_VALUE) {
            expectedSize ++;
        }
    }

}
