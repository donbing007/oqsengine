package com.xforceplus.ultraman.oqsengine.cdc.cdcerrors;

import com.xforceplus.ultraman.oqsengine.cdc.CDCAbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import org.junit.*;
import org.junit.runner.RunWith;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.UN_KNOW_OP;

/**
 * desc :
 * name : SQLDevOpsStorageTest
 *
 * @author : xujia
 * date : 2020/11/22
 * @since : 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS, ContainerType.MYSQL, ContainerType.MANTICORE})
public class CdcErrorStorageTest extends CDCAbstractContainer {

    private static long unExpectedSeqNo = Long.MAX_VALUE;
    private static long unExpectedId = Long.MAX_VALUE;
    private static long expectedBatchId = 1L;
    private static long expectedEntityId = 2L;
    private static int expectedOp = UN_KNOW_OP;
    private static int expectedVersion = 3;
    private static long expectedSeqNo = 4L;
    private static long expectedId = 5L;
    private static long expectedCommitId = 6L;
    private static String expectedMessage = "cdc sync error";
    private static int expectedErrorType = ErrorType.DATA_FORMAT_ERROR.getType();

    private static String expectedObjectStr = "111";

    private static CdcErrorTask expectedCdcErrorTask =
                CdcErrorTask.buildErrorTask(expectedSeqNo, expectedBatchId, expectedId, expectedEntityId
                        , expectedVersion, expectedOp, expectedCommitId, expectedErrorType, "2", expectedMessage);


    @BeforeClass
    public static void beforeClass() {
        ContainerStarter.startMysql();
        ContainerStarter.startManticore();
        ContainerStarter.startRedis();
        ContainerStarter.startCannal();
    }

    @AfterClass
    public static void afterClass() {
        ContainerStarter.reset();
    }


    @Before
    public void before() throws Exception {
        initAll(false);
    }

    @After
    public void after() throws SQLException {
        closeAll();
    }

    private DataSource buildDevOpsDataSource() {
        return dataSourcePackage.getDevOps();
    }


    @Test
    public void cdcCRU() throws Exception {
        int count = cdcErrorStorage.buildCdcError(expectedCdcErrorTask);
        Assert.assertEquals(1, count);

        count = cdcErrorStorage.submitRecover(expectedCdcErrorTask.getSeqNo(), FixedStatus.SUBMIT_FIX_REQ, expectedObjectStr);
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
        cdcErrorQueryCondition.setStatus(FixedStatus.FIXED.getStatus());
        queryWithOneExpected(cdcErrorQueryCondition);

        //  使用expectedCommitId查询
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setCommitId(expectedCommitId);
        queryWithOneExpected(cdcErrorQueryCondition);

        //使用batchId和NOT_FIXED 且 isEquals = false
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(expectedBatchId).setId(null).setCommitId(null).setType(ErrorType.DATA_FORMAT_ERROR.getType()).setStatus(FixedStatus.NOT_FIXED.getStatus()).setEqualStatus(false);
        queryWithOneExpected(cdcErrorQueryCondition);

        //使用所有条件查询
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(expectedBatchId)
                .setId(expectedId).setCommitId(expectedCommitId).setType(ErrorType.DATA_FORMAT_ERROR.getType()).setStatus(FixedStatus.NOT_FIXED.getStatus()).setEqualStatus(false);
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
        Assert.assertEquals(expectedBatchId, cdcErrorTask.getBatchId());
        Assert.assertEquals(expectedId, cdcErrorTask.getId());
        Assert.assertEquals(expectedEntityId, cdcErrorTask.getEntity());
        Assert.assertEquals(expectedVersion, cdcErrorTask.getVersion());
        Assert.assertEquals(expectedOp, cdcErrorTask.getOp());
        Assert.assertEquals(expectedCommitId, cdcErrorTask.getCommitId());
        Assert.assertEquals(expectedErrorType, cdcErrorTask.getErrorType());
        Assert.assertEquals(expectedMessage, cdcErrorTask.getMessage());
        Assert.assertEquals(expectedObjectStr, cdcErrorTask.getOperationObject());
        Assert.assertEquals(FixedStatus.FIXED.getStatus(), cdcErrorTask.getStatus());
        Assert.assertTrue(System.currentTimeMillis() > cdcErrorTask.getExecuteTime() && cdcErrorTask.getExecuteTime() > 0);
        Assert.assertTrue(System.currentTimeMillis() > cdcErrorTask.getFixedTime() && cdcErrorTask.getFixedTime() > 0);
    }
}
