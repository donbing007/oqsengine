package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.CdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.helper.CdcErrorBuildHelper;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
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
public class CdcErrorUpdateStatusExecutorTest extends AbstractCdcHelper {
    private String expectedUpdate = "UPDATE cdcerrors SET status=?, fixedtime=? WHERE seqno=?";

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
    public void errorUpdateStatusTest() throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();

        cdcErrorStorage.buildCdcError(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASK);

        long expectedSeqNo = CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASK.getSeqNo();


        int count = CdcErrorUpdateStatusExecutor
            .build(CdcInitialization.CDC_ERRORS, CdcInitialization.getInstance().getDevOpsDataSource(), 10_000L, FixedStatus.FIXED)
            .execute(expectedSeqNo);

        Assertions.assertEquals(1, count);

        CdcErrorQueryCondition cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setSeqNo(expectedSeqNo);

        Collection<CdcErrorTask> cdcErrorTaskList = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assertions.assertEquals(1, cdcErrorTaskList.size());
        Assertions.assertEquals(FixedStatus.FIXED.getStatus(), ((List<CdcErrorTask>) cdcErrorTaskList).get(0).getStatus());
    }

    @Test
    public void updateSqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CdcErrorUpdateStatusExecutor cdcErrorUpdateExecutor =
            new CdcErrorUpdateStatusExecutor(CdcInitialization.CDC_ERRORS, null, 0, FixedStatus.FIXED);
        Method m = cdcErrorUpdateExecutor.getClass()
            .getDeclaredMethod("buildSQL", new Class[] {});
        m.setAccessible(true);

        String result = (String) m.invoke(cdcErrorUpdateExecutor, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedUpdate, result);
    }


}
