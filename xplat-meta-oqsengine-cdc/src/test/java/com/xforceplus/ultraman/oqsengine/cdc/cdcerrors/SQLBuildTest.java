package com.xforceplus.ultraman.oqsengine.cdc.cdcerrors;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorBuildExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorQueryExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorRecoverExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorUpdateExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * desc :.
 * name : SQLBuildTest
 *
 * @author : xujia 2020/11/22
 * @since : 1.8
 */
public class SQLBuildTest {
    private String tableName = "cdcerrors";

    private String expectedBuild =
        "INSERT INTO cdcerrors (seqno,unikey,batchid,id,entity,version,op,commitid,type,status,operationobject,message,executetime,fixedtime) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private String expectedUpdate = "UPDATE cdcerrors SET status=?, fixedtime=? WHERE seqno=?";
    private String expectedRecover = "UPDATE cdcerrors SET status=?, operationobject=? WHERE seqno=?";
    private String expectedFullSelect =
        "SELECT seqno,unikey,batchid,id,entity,version,op,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors WHERE seqno=? AND unikey=? AND batchid=? AND id=? AND commitid=? AND type=? AND status=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=? order by executetime desc";
    private String expectedFullNotEqualStatusSelect =
        "SELECT seqno,unikey,batchid,id,entity,version,op,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors WHERE seqno=? AND unikey=? AND batchid=? AND id=? AND commitid=? AND type=? AND status!=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=? order by executetime desc";

    private String expectedEmptySelect =
        "SELECT seqno,unikey,batchid,id,entity,version,op,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors order by executetime desc";
    private String expectedIdSelect =
        "SELECT seqno,unikey,batchid,id,entity,version,op,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors WHERE id=? order by executetime desc";

    @Test
    public void buildSqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CdcErrorBuildExecutor cdcErrorBuildExecutor = new CdcErrorBuildExecutor(tableName, null, 0);
        Method m = cdcErrorBuildExecutor.getClass()
            .getDeclaredMethod("buildSQL", new Class[] {});
        m.setAccessible(true);

        String result = (String) m.invoke(cdcErrorBuildExecutor, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedBuild, result);
    }

    @Test
    public void updateSqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CdcErrorUpdateExecutor cdcErrorUpdateExecutor =
            new CdcErrorUpdateExecutor(tableName, null, 0, FixedStatus.FIXED);
        Method m = cdcErrorUpdateExecutor.getClass()
            .getDeclaredMethod("buildSQL", new Class[] {});
        m.setAccessible(true);

        String result = (String) m.invoke(cdcErrorUpdateExecutor, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedUpdate, result);
    }

    @Test
    public void recoverSqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CdcErrorRecoverExecutor cdcErrorRecoverExecutor =
            new CdcErrorRecoverExecutor(tableName, null, 0, FixedStatus.SUBMIT_FIX_REQ, "1");
        Method m = cdcErrorRecoverExecutor.getClass()
            .getDeclaredMethod("buildSQL", new Class[] {});
        m.setAccessible(true);

        String result = (String) m.invoke(cdcErrorRecoverExecutor, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedRecover, result);
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

        CdcErrorQueryExecutor cdcErrorQueryExecutor = new CdcErrorQueryExecutor(tableName, null, 0);
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

        CdcErrorQueryExecutor cdcErrorQueryExecutor = new CdcErrorQueryExecutor(tableName, null, 0);
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

        CdcErrorQueryExecutor cdcErrorQueryExecutor = new CdcErrorQueryExecutor(tableName, null, 0);
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
