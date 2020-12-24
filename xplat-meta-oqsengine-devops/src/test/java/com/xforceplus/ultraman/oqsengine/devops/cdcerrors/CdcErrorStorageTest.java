package com.xforceplus.ultraman.oqsengine.devops.cdcerrors;

import com.xforceplus.ultraman.oqsengine.devops.DevOpsAbstractContainer;
import com.xforceplus.ultraman.oqsengine.devops.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerHelper;
import org.junit.*;

import java.sql.SQLException;
import java.util.Collection;

/**
 * desc :
 * name : SQLDevOpsStorageTest
 *
 * @author : xujia
 * date : 2020/11/22
 * @since : 1.8
 */
public class CdcErrorStorageTest extends DevOpsAbstractContainer {

    private static long unExpectedSeqNo = Long.MAX_VALUE;
    private static long unExpectedId = Long.MAX_VALUE;
    private static long expectedSeqNo = 1L;
    private static long expectedId = 2L;
    private static long expectedCommitId = 3L;
    private static String expectedMessage = "cdc sync error";

    private static CdcErrorTask expectedCdcErrorTask =
                CdcErrorTask.buildErrorTask(expectedSeqNo, expectedId, expectedCommitId, expectedMessage);

    @BeforeClass
    public static void beforeClass() {
        ContainerHelper.startMysql();
        ContainerHelper.startManticore();
        ContainerHelper.startRedis();
    }

    @Before
    public void before() throws Exception {
        start();
    }

    @After
    public void after() throws SQLException {
        clear();
        close();
    }

    @Test
    public void cdcCRU() throws Exception {
        int count = cdcErrorStorage.buildCdcError(expectedCdcErrorTask);
        Assert.assertEquals(1, count);

        count = cdcErrorStorage.updateCdcError(expectedCdcErrorTask.getSeqNo(), FixedStatus.FIXED);
        Assert.assertEquals(1, count);

        count = cdcErrorStorage.updateCdcError(unExpectedSeqNo, FixedStatus.FIXED);
        Assert.assertEquals(0, count);

        //  使用expectedSeqNo查询
        CdcErrorQueryCondition cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setSeqNo(expectedSeqNo);
        queryWithOneExpected(cdcErrorQueryCondition);
        //  未查询到
        cdcErrorQueryCondition.setSeqNo(unExpectedSeqNo);
        queryWithUnexpected(cdcErrorQueryCondition);

        //  使用expectedId查询
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setId(expectedId);
        queryWithOneExpected(cdcErrorQueryCondition);
        //  未查询到
        cdcErrorQueryCondition.setId(unExpectedId);
        queryWithUnexpected(cdcErrorQueryCondition);

        //  使用FixedStatus查询
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setStatus(FixedStatus.FIXED.ordinal());
        queryWithOneExpected(cdcErrorQueryCondition);

        //  使用expectedCommitId查询
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setCommitId(expectedCommitId);
        queryWithOneExpected(cdcErrorQueryCondition);
    }

    private void queryWithOneExpected(CdcErrorQueryCondition cdcErrorQueryCondition) throws SQLException {
        Collection<CdcErrorTask> cdcErrorTaskList = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assert.assertEquals(1, cdcErrorTaskList.size());
        isExpectedCdcErrorTask(cdcErrorTaskList.stream().findFirst().get());
    }

    private void queryWithUnexpected(CdcErrorQueryCondition cdcErrorQueryCondition) throws SQLException {
        Collection<CdcErrorTask> cdcErrorTaskList = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assert.assertEquals(0, cdcErrorTaskList.size());
    }

    private void isExpectedCdcErrorTask(CdcErrorTask cdcErrorTask) {
        Assert.assertEquals(expectedSeqNo, cdcErrorTask.getSeqNo());
        Assert.assertEquals(expectedId, cdcErrorTask.getId());
        Assert.assertEquals(expectedCommitId, cdcErrorTask.getCommitId());
        Assert.assertEquals(expectedMessage, cdcErrorTask.getMessage());
        Assert.assertEquals(FixedStatus.FIXED.ordinal(), cdcErrorTask.getStatus());
        Assert.assertTrue(System.currentTimeMillis() > cdcErrorTask.getExecuteTime() && cdcErrorTask.getExecuteTime() > 0);
        Assert.assertTrue(System.currentTimeMillis() > cdcErrorTask.getFixedTime() && cdcErrorTask.getFixedTime() > 0);
    }
}
