package com.xforceplus.ultraman.oqsengine.cdc.cdcerror;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.helper.CdcErrorBuildHelper;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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
public class SQLCdcErrorStorageTest extends AbstractCdcHelper {

    @BeforeEach
    public void before() throws Exception {
        super.init(false, null);
    }

    @AfterEach
    public void after() throws Exception {
        super.clear(false);
    }

    @AfterAll
    public static void afterAll() {
        InitializationHelper.destroy();
    }

    @Test
    public void buildCdcErrorTest() throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();

        int count = cdcErrorStorage.buildCdcError(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASK);

        Assertions.assertEquals(1, count);
    }

    @Test
    public void queryCdcErrorsByConditionTest() throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();

        cdcErrorStorage.buildCdcError(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASK);

        //  使用expectedSeqNo查询
        CdcErrorQueryCondition cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setSeqNo(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASK.getSeqNo());
        CdcErrorBuildHelper.queryWithExpected(cdcErrorQueryCondition, FixedStatus.NOT_FIXED);

        //  未查询到
        cdcErrorQueryCondition.setSeqNo(CdcErrorBuildHelper.unExpectedSeqNo);
        CdcErrorBuildHelper.queryWithUnexpected(cdcErrorQueryCondition);

        //  使用expectedId查询
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setId(CdcErrorBuildHelper.expectedId);
        CdcErrorBuildHelper.queryWithExpected(cdcErrorQueryCondition, FixedStatus.NOT_FIXED);

        //  未查询到
        cdcErrorQueryCondition.setId(CdcErrorBuildHelper.unExpectedId);
        CdcErrorBuildHelper.queryWithUnexpected(cdcErrorQueryCondition);

        //  使用FixedStatus查询
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setStatus(FixedStatus.NOT_FIXED.getStatus());
        CdcErrorBuildHelper.queryWithExpected(cdcErrorQueryCondition, FixedStatus.NOT_FIXED);

        //  使用expectedCommitId查询
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setCommitId(CdcErrorBuildHelper.expectedCommitId);
        CdcErrorBuildHelper.queryWithExpected(cdcErrorQueryCondition, FixedStatus.NOT_FIXED);

        //  使用unikey
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setUniKey(CdcErrorBuildHelper.expectedUniKey);
        CdcErrorBuildHelper.queryWithExpected(cdcErrorQueryCondition, FixedStatus.NOT_FIXED);

        //使用batchId和NOT_FIXED 且 isEquals = true
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(CdcErrorBuildHelper.expectedBatchId).setId(null).setCommitId(null)
            .setType(ErrorType.DATA_FORMAT_ERROR.getType()).setStatus(FixedStatus.NOT_FIXED.getStatus())
            .setEqualStatus(true);
        CdcErrorBuildHelper.queryWithExpected(cdcErrorQueryCondition, FixedStatus.NOT_FIXED);

        //使用batchId和NOT_FIXED 且 isEquals = false
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(CdcErrorBuildHelper.expectedBatchId).setId(null).setCommitId(null)
            .setType(ErrorType.DATA_FORMAT_ERROR.getType()).setStatus(FixedStatus.NOT_FIXED.getStatus())
            .setEqualStatus(false);
        CdcErrorBuildHelper.queryWithUnexpected(cdcErrorQueryCondition);

        //使用所有条件查询
        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(CdcErrorBuildHelper.expectedBatchId)
            .setId(CdcErrorBuildHelper.expectedId).setCommitId(CdcErrorBuildHelper.expectedCommitId)
            .setType(ErrorType.DATA_FORMAT_ERROR.getType())
            .setStatus(FixedStatus.NOT_FIXED.getStatus()).setEqualStatus(true);
        CdcErrorBuildHelper.queryWithExpected(cdcErrorQueryCondition, FixedStatus.NOT_FIXED);
    }

    @Test
    public void queryCdcErrorsByUniKeyTest() throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();
        Assertions.assertTrue(cdcErrorStorage.batchInsert(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASKS));

        List<String>
            keys = CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASKS.stream().map(CdcErrorTask::getUniKey).collect(
            Collectors.toList());

        Collection<CdcErrorTask> cdcErrorTasks = cdcErrorStorage.queryCdcErrors(keys);

        CdcErrorBuildHelper.checkBatches(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASKS, cdcErrorTasks);
    }

    @Test
    public void batchTest() throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();
        Assertions.assertTrue(cdcErrorStorage.batchInsert(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASKS));

        List<String>
            keys = CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASKS.stream().map(CdcErrorTask::getUniKey).collect(
            Collectors.toList());

        Collection<CdcErrorTask> cdcErrorTasks = cdcErrorStorage.queryCdcErrors(keys);

        Assertions.assertEquals(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASKS.size(), cdcErrorTasks.size());
    }

    @Test
    public void updateCdcErrorTest() throws Exception {

        int updateIndex = 3;

        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();
        Assertions.assertTrue(cdcErrorStorage.batchInsert(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASKS));

        long seqNo = CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASKS.get(updateIndex).getSeqNo();

        cdcErrorStorage.updateCdcErrorStatus(seqNo, FixedStatus.FIXED);

        CdcErrorQueryCondition cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setSeqNo(seqNo);

        Collection<CdcErrorTask> cdcErrorTasks = CdcErrorBuildHelper.queryWithExpected(cdcErrorQueryCondition);
        for (CdcErrorTask cdcErrorTask : cdcErrorTasks) {
            Assertions.assertEquals(FixedStatus.FIXED.getStatus(), cdcErrorTask.getStatus());
        }
    }

}
