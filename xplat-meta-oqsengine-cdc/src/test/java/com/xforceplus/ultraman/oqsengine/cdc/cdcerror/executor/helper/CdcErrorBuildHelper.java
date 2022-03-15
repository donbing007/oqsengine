package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.helper;

import static com.xforceplus.ultraman.oqsengine.cdc.cdcerror.tools.CdcErrorUtils.uniKeyGenerate;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.UN_KNOW_OP;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.CdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class CdcErrorBuildHelper {

    public static final long unExpectedSeqNo = Long.MAX_VALUE;
    public static final long unExpectedId = Long.MAX_VALUE;
    public static final long expectedBatchId = 1L;
    public static final long expectedEntityId = 2L;
    public static final int expectedOp = UN_KNOW_OP;
    public static final int expectedVersion = 3;
    public static final long expectedSeqNo = 4L;
    public static final long expectedId = 5L;
    public static final long expectedCommitId = 6L;
    public static final String expectedMessage = "cdc sync error";
    public static final int expectedErrorType = ErrorType.DATA_FORMAT_ERROR.getType();

    public static final String expectedObjectStr = "2";

    public static final String expectedUniKey = uniKeyGenerate("111", 1, ErrorType.DATA_INSERT_ERROR);

    public static final CdcErrorTask EXPECTED_CDC_ERROR_TASK =
        CdcErrorTask.buildErrorTask(expectedSeqNo, expectedUniKey, expectedBatchId, expectedId, expectedEntityId,
            expectedVersion, expectedOp, expectedCommitId, expectedErrorType, expectedObjectStr, expectedMessage);

    public static void queryWithExpected(CdcErrorQueryCondition cdcErrorQueryCondition, FixedStatus expectedFixedStatus) throws Exception {
        Collection<CdcErrorTask> cdcErrorTasks = queryWithExpected(cdcErrorQueryCondition);

        check(cdcErrorTasks, expectedFixedStatus);
    }

    public static void check(Collection<CdcErrorTask> cdcErrorTaskList, FixedStatus expectedFixedStatus) {
        for (CdcErrorTask cdcErrorTask : cdcErrorTaskList) {
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
            Assertions.assertEquals(expectedFixedStatus.getStatus(), cdcErrorTask.getStatus());
            Assertions.assertTrue(
                System.currentTimeMillis() > cdcErrorTask.getExecuteTime() && cdcErrorTask.getExecuteTime() > 0);
            if (expectedFixedStatus.equals(FixedStatus.FIXED)) {
                Assertions.assertTrue(
                    System.currentTimeMillis() > cdcErrorTask.getFixedTime() && cdcErrorTask.getFixedTime() > 0);
            }
        }
    }

    public static void checkBatches(List<CdcErrorTask> expectedList, Collection<CdcErrorTask> actualList) {
        Assertions.assertEquals(expectedList.size(), actualList.size());
        for (CdcErrorTask actual : actualList) {
            Optional<CdcErrorTask> eOp = expectedList.stream().filter(c -> {
                return c.getSeqNo() == actual.getSeqNo();
            }).findFirst();

            Assertions.assertTrue(eOp.isPresent());
            CdcErrorTask expected = eOp.get();
            Assertions.assertEquals(expected.toString(), actual.toString());
        }
    }

    public static void queryWithUnexpected(CdcErrorQueryCondition cdcErrorQueryCondition) throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();
        Collection<CdcErrorTask> cdcErrorTaskList = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assertions.assertEquals(0, cdcErrorTaskList.size());
    }

    public static Collection<CdcErrorTask> queryWithExpected(CdcErrorQueryCondition cdcErrorQueryCondition) throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();
        Collection<CdcErrorTask> cdcErrorTaskList = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assertions.assertEquals(1, cdcErrorTaskList.size());

        return cdcErrorTaskList;
    }


    public static final List<CdcErrorTask> EXPECTED_CDC_ERROR_TASKS =
        Arrays.asList(
            CdcErrorTask.buildErrorTask(1, "1", 1, 1, 1,
                1, 1, 1, FixedStatus.NOT_FIXED.getStatus(), "1", "1"),
            CdcErrorTask.buildErrorTask(2, "2", 2, 2, 2,
                2, 2, 2, FixedStatus.NOT_FIXED.getStatus(), "2", "2"),
            CdcErrorTask.buildErrorTask(3, "3", 3, 3, 3,
                3, 3, 3, FixedStatus.NOT_FIXED.getStatus(), "3", "3"),
            CdcErrorTask.buildErrorTask(5, "5", 5, 5, 5,
                5, 5, 5, FixedStatus.NOT_FIXED.getStatus(), "5", "5")
        );
}
