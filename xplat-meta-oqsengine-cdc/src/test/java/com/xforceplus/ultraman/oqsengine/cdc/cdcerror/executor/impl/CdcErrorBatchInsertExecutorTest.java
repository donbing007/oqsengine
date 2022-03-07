package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
public class CdcErrorBatchInsertExecutorTest extends AbstractCdcHelper {
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


    private static final String expectedSql =
        "INSERT INTO cdcerrors (seqno,unikey,batchid,id,entity,version,op,commitid,type,status,operationobject,message,executetime,fixedtime) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


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
    public void errorBatchBuildTest() throws Exception {
        Assertions.assertTrue(CdcErrorBatchInsertExecutor
            .build(CdcInitialization.CDC_ERRORS, CdcInitialization.getInstance().getDevOpsDataSource(), 10_000L)
            .execute(EXPECTED_CDC_ERROR_TASKS));
    }

    @Test
    public void buildSqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CdcErrorBuildExecutor cdcErrorBuildExecutor = new CdcErrorBuildExecutor(CdcInitialization.CDC_ERRORS, null, 0);
        Method m = cdcErrorBuildExecutor.getClass()
            .getDeclaredMethod("buildSQL", new Class[] {});
        m.setAccessible(true);

        String result = (String) m.invoke(cdcErrorBuildExecutor, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedSql, result);
    }
}
