package com.xforceplus.ultraman.oqsengine.cdc.cdcerrors;

import static com.xforceplus.ultraman.oqsengine.cdc.cdcerror.tools.CdcErrorUtils.uniKeyGenerate;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.UN_KNOW_OP;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractCDCTestHelper;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.CdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * desc :.
 * name : SQLDevOpsStorageTest
 *
 * @author : xujia 2020/11/22
 * @since : 1.8
 */
@Disabled("暂时关闭测试, 测试不完善.")
public class CdcErrorStorageTest extends AbstractCDCTestHelper {

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

    private static String expectedUniKey = uniKeyGenerate("111", 1, ErrorType.DATA_INSERT_ERROR);

    private static CdcErrorTask expectedCdcErrorTask =
        CdcErrorTask.buildErrorTask(expectedSeqNo, expectedUniKey, expectedBatchId, expectedId, expectedEntityId,
            expectedVersion, expectedOp, expectedCommitId, expectedErrorType, "2", expectedMessage);


    @BeforeEach
    public void before() throws Exception {
        super.init(false);
    }

    @AfterEach
    public void after() throws Exception {
        super.destroy(false);
    }


    @Test
    public void cdcCru() throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();

        int count = cdcErrorStorage.buildCdcError(expectedCdcErrorTask);
        Assertions.assertEquals(1, count);

        count = cdcErrorStorage
            .submitRecover(expectedCdcErrorTask.getSeqNo(), FixedStatus.SUBMIT_FIX_REQ, expectedObjectStr);
        Assertions.assertEquals(1, count);

        count = cdcErrorStorage.updateCdcError(expectedCdcErrorTask.getSeqNo(), FixedStatus.FIXED);
        Assertions.assertEquals(1, count);

        count = cdcErrorStorage.updateCdcError(unExpectedSeqNo, FixedStatus.FIXED);
        Assertions.assertEquals(0, count);


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

        //  使用unikey
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setUniKey(expectedUniKey);
        queryWithOneExpected(cdcErrorQueryCondition);

        //使用batchId和NOT_FIXED 且 isEquals = false
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(expectedBatchId).setId(null).setCommitId(null)
            .setType(ErrorType.DATA_FORMAT_ERROR.getType()).setStatus(FixedStatus.NOT_FIXED.getStatus())
            .setEqualStatus(false);
        queryWithOneExpected(cdcErrorQueryCondition);

        //使用所有条件查询
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(expectedBatchId)
            .setId(expectedId).setCommitId(expectedCommitId).setType(ErrorType.DATA_FORMAT_ERROR.getType())
            .setStatus(FixedStatus.NOT_FIXED.getStatus()).setEqualStatus(false);
        queryWithOneExpected(cdcErrorQueryCondition);
    }

    private void queryWithOneExpected(CdcErrorQueryCondition cdcErrorQueryCondition) throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();
        Collection<CdcErrorTask> cdcErrorTaskList = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assertions.assertEquals(1, cdcErrorTaskList.size());
        isExpectedCdcErrorTask(cdcErrorTaskList.stream().findFirst().get());
    }

    private void queryWithUnexpected(CdcErrorQueryCondition cdcErrorQueryCondition) throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();
        Collection<CdcErrorTask> cdcErrorTaskList = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assertions.assertEquals(0, cdcErrorTaskList.size());
    }

    private void isExpectedCdcErrorTask(CdcErrorTask cdcErrorTask) {
        Assertions.assertEquals(expectedSeqNo, cdcErrorTask.getSeqNo());
        Assertions.assertEquals(expectedUniKey, cdcErrorTask.getUniKey());
        Assertions.assertEquals(expectedBatchId, cdcErrorTask.getBatchId());
        Assertions.assertEquals(expectedId, cdcErrorTask.getId());
        Assertions.assertEquals(expectedEntityId, cdcErrorTask.getEntity());
        Assertions.assertEquals(expectedVersion, cdcErrorTask.getVersion());
        Assertions.assertEquals(expectedOp, cdcErrorTask.getOp());
        Assertions.assertEquals(expectedCommitId, cdcErrorTask.getCommitId());
        Assertions.assertEquals(expectedErrorType, cdcErrorTask.getErrorType());
        Assertions.assertEquals(expectedMessage, cdcErrorTask.getMessage());
        Assertions.assertEquals(expectedObjectStr, cdcErrorTask.getOperationObject());
        Assertions.assertEquals(FixedStatus.FIXED.getStatus(), cdcErrorTask.getStatus());
        Assertions.assertTrue(
            System.currentTimeMillis() > cdcErrorTask.getExecuteTime() && cdcErrorTask.getExecuteTime() > 0);
        Assertions.assertTrue(System.currentTimeMillis() > cdcErrorTask.getFixedTime() && cdcErrorTask.getFixedTime() > 0);
    }
}
