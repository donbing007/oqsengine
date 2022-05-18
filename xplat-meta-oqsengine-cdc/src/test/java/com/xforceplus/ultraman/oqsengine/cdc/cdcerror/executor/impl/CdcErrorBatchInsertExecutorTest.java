package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.helper.CdcErrorBuildHelper;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        try {
            InitializationHelper.destroy();
        } catch (Exception e) {

        }
    }

    @Test
    public void errorBatchBuildTest() throws Exception {
        Assertions.assertTrue(CdcErrorBatchInsertExecutor
            .build(CdcInitialization.CDC_ERRORS, CdcInitialization.getInstance().getDevOpsDataSource(), 10_000L)
            .execute(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASKS));
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
