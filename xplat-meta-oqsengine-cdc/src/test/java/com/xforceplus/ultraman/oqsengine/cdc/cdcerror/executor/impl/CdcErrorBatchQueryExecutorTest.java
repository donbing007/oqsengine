package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.CdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.helper.CdcErrorBuildHelper;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
public class CdcErrorBatchQueryExecutorTest  extends AbstractCdcHelper {

    private String expectedSql =
        "SELECT seqno,unikey,batchid,id,entity,version,op,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors WHERE unikey IN (%s) order by executetime desc";


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
    public void errorBatchQueryTest() throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();
        Assertions.assertTrue(cdcErrorStorage.batchInsert(CdcErrorBatchInsertExecutorTest.EXPECTED_CDC_ERROR_TASKS));

        List<String> keys = CdcErrorBatchInsertExecutorTest.EXPECTED_CDC_ERROR_TASKS.stream().map(CdcErrorTask::getUniKey).collect(
            Collectors.toList());

        Collection<CdcErrorTask> cdcErrorTasks = CdcErrorBatchQueryExecutor
            .build(CdcInitialization.CDC_ERRORS, CdcInitialization.getInstance().getDevOpsDataSource(), 10_000L)
            .execute(keys);

        CdcErrorBuildHelper.checkBatches(CdcErrorBatchInsertExecutorTest.EXPECTED_CDC_ERROR_TASKS, cdcErrorTasks);
    }



    @Test
    public void buildSqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CdcErrorBatchQueryExecutor cdcErrorBatchQueryExecutor =
            new CdcErrorBatchQueryExecutor(CdcInitialization.CDC_ERRORS, null, 0);
        Method m = cdcErrorBatchQueryExecutor.getClass()
            .getDeclaredMethod("buildSQL", new Class[] {});
        m.setAccessible(true);

        String result = (String) m.invoke(cdcErrorBatchQueryExecutor, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedSql, result);
    }
}
