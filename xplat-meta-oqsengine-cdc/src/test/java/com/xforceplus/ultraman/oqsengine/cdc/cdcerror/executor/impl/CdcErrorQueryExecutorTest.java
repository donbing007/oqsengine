package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.CdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.helper.CdcErrorBuildHelper;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
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
public class CdcErrorQueryExecutorTest extends AbstractCdcHelper {
    private String expectedFullSelect =
        "SELECT seqno,unikey,batchid,id,entity,version,op,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors WHERE seqno=? AND unikey=? AND batchid=? AND id=? AND commitid=? AND type=? AND status=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=? order by executetime desc";
    private String expectedFullNotEqualStatusSelect =
        "SELECT seqno,unikey,batchid,id,entity,version,op,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors WHERE seqno=? AND unikey=? AND batchid=? AND id=? AND commitid=? AND type=? AND status!=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=? order by executetime desc";

    private String expectedEmptySelect =
        "SELECT seqno,unikey,batchid,id,entity,version,op,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors order by executetime desc";
    private String expectedIdSelect =
        "SELECT seqno,unikey,batchid,id,entity,version,op,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors WHERE id=? order by executetime desc";


    @BeforeEach
    public void before() throws Exception {
        super.init(false, null);
    }

    @AfterEach
    public void after() throws Exception {
        super.clear(false);
    }

    @Test
    public void errorQueryTest() throws Exception {
        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();

        cdcErrorStorage.buildCdcError(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASK);

        //  使用expectedSeqNo查询
        CdcErrorQueryCondition cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setSeqNo(CdcErrorBuildHelper.EXPECTED_CDC_ERROR_TASK.getSeqNo());

        Collection<CdcErrorTask> cdcErrorTasks = CdcErrorQueryExecutor
            .build(CdcInitialization.CDC_ERRORS, CdcInitialization.getInstance().getDevOpsDataSource(), 10_000L)
            .execute(cdcErrorQueryCondition);

        CdcErrorBuildHelper.check(cdcErrorTasks, FixedStatus.NOT_FIXED);
    }

    @Test
    public void fullQuerySqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        checkByCondition(true, expectedFullSelect);
    }

    @Test
    public void expectedFullNotEqualStatusSelectSqlTest()
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        checkByCondition(false, expectedFullNotEqualStatusSelect);
    }

    @Test
    public void queryIdConditionSqlTest()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        CdcErrorQueryCondition expectErrorQueryCondition = new CdcErrorQueryCondition();
        expectErrorQueryCondition.setId(1L);

        CdcErrorQueryExecutor cdcErrorQueryExecutor = new CdcErrorQueryExecutor(CdcInitialization.CDC_ERRORS, null, 0);
        Method m = cdcErrorQueryExecutor.getClass()
            .getDeclaredMethod("buildSQL", new Class[] {StringBuilder.class, CdcErrorQueryCondition.class});
        m.setAccessible(true);

        StringBuilder stringBuilder = new StringBuilder();
        Boolean result = (Boolean) m.invoke(cdcErrorQueryExecutor, stringBuilder, expectErrorQueryCondition);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result);

        Assertions.assertEquals(expectedIdSelect, stringBuilder.toString());
    }

    @Test
    public void emptyQuerySqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        CdcErrorQueryCondition expectErrorQueryCondition = new CdcErrorQueryCondition();

        CdcErrorQueryExecutor cdcErrorQueryExecutor = new CdcErrorQueryExecutor(CdcInitialization.CDC_ERRORS, null, 0);
        Method m = cdcErrorQueryExecutor.getClass()
            .getDeclaredMethod("buildSQL", new Class[] {StringBuilder.class, CdcErrorQueryCondition.class});
        m.setAccessible(true);

        StringBuilder stringBuilder = new StringBuilder();
        Boolean result = (Boolean) m.invoke(cdcErrorQueryExecutor, stringBuilder, expectErrorQueryCondition);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result);

        Assertions.assertEquals(expectedEmptySelect, stringBuilder.toString());
    }

    private void checkByCondition(boolean isEquals, String expectString)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CdcErrorQueryCondition expectErrorQueryCondition = init(isEquals);

        CdcErrorQueryExecutor cdcErrorQueryExecutor = new CdcErrorQueryExecutor(CdcInitialization.CDC_ERRORS, null, 0);
        Method m = cdcErrorQueryExecutor.getClass()
            .getDeclaredMethod("buildSQL", new Class[] {StringBuilder.class, CdcErrorQueryCondition.class});
        m.setAccessible(true);

        StringBuilder stringBuilder = new StringBuilder();
        Boolean result = (Boolean) m.invoke(cdcErrorQueryExecutor, stringBuilder, expectErrorQueryCondition);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result);

        Assertions.assertEquals(expectString, stringBuilder.toString());
    }

    private CdcErrorQueryCondition init(boolean isEquals) {
        CdcErrorQueryCondition expectErrorQueryCondition = new CdcErrorQueryCondition();
        expectErrorQueryCondition.setSeqNo(1L);
        expectErrorQueryCondition.setUniKey("aaaa");
        expectErrorQueryCondition.setBatchId(1L);
        expectErrorQueryCondition.setId(Long.MAX_VALUE);
        expectErrorQueryCondition.setCommitId(2L);
        expectErrorQueryCondition.setRangeLeExecuteTime(99L);
        expectErrorQueryCondition.setRangeGeExecuteTime(1L);
        expectErrorQueryCondition.setRangeLeFixedTime(200L);
        expectErrorQueryCondition.setRangeGeFixedTime(100L);
        expectErrorQueryCondition.setType(ErrorType.DATA_FORMAT_ERROR.getType());
        expectErrorQueryCondition.setStatus(FixedStatus.FIXED.getStatus());
        if (!isEquals) {
            expectErrorQueryCondition.setEqualStatus(false);
        }

        return expectErrorQueryCondition;
    }
}
