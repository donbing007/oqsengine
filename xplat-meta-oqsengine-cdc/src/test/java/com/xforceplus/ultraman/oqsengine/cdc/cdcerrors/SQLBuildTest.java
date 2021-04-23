package com.xforceplus.ultraman.oqsengine.cdc.cdcerrors;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorBuildExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorQueryExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorRecoverExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.executor.impl.CdcErrorUpdateExecutor;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * desc :
 * name : SQLBuildTest
 *
 * @author : xujia
 * date : 2020/11/22
 * @since : 1.8
 */
public class SQLBuildTest {
    private String tableName = "cdcerrors";

    private String expectedBuild = "INSERT INTO cdcerrors (seqno,batchid,id,commitid,type,status,operationobject,message,executetime,fixedtime) VALUES (?,?,?,?,?,?,?,?,?,?)";
    private String expectedUpdate = "UPDATE cdcerrors SET status=?, fixedtime=? WHERE seqno=?";
    private String expectedRecover = "UPDATE cdcerrors SET status=?, operationobject=? WHERE seqno=?";
    private String expectedFullSelect = "SELECT seqno,batchid,id,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors WHERE seqno=? AND batchid=? AND id=? AND commitid=? AND type=? AND status=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=? order by executetime desc";
    private String expectedFullNotEqualStatusSelect = "SELECT seqno,batchid,id,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors WHERE seqno=? AND batchid=? AND id=? AND commitid=? AND type=? AND status!=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=? order by executetime desc";

    private String expectedEmptySelect = "SELECT seqno,batchid,id,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors order by executetime desc";
    private String expectedIdSelect = "SELECT seqno,batchid,id,commitid,type,status,operationobject,message,executetime,fixedtime FROM cdcerrors WHERE id=? order by executetime desc";

    @Test
    public void buildSqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CdcErrorBuildExecutor cdcErrorBuildExecutor = new CdcErrorBuildExecutor(tableName, null, 0);
        Method m = cdcErrorBuildExecutor.getClass()
                .getDeclaredMethod("buildSQL", new Class[]{});
        m.setAccessible(true);

        String result = (String) m.invoke(cdcErrorBuildExecutor, null);

        Assert.assertNotNull(result);
        Assert.assertEquals(expectedBuild, result);
    }

    @Test
    public void UpdateSqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CdcErrorUpdateExecutor cdcErrorUpdateExecutor = new CdcErrorUpdateExecutor(tableName, null, 0, FixedStatus.FIXED);
        Method m = cdcErrorUpdateExecutor.getClass()
                .getDeclaredMethod("buildSQL", new Class[]{});
        m.setAccessible(true);

        String result = (String) m.invoke(cdcErrorUpdateExecutor, null);

        Assert.assertNotNull(result);
        Assert.assertEquals(expectedUpdate, result);
    }

    @Test
    public void recoverSqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CdcErrorRecoverExecutor cdcErrorRecoverExecutor = new CdcErrorRecoverExecutor(tableName, null, 0, FixedStatus.SUBMIT_FIX_REQ, "1");
        Method m = cdcErrorRecoverExecutor.getClass()
                .getDeclaredMethod("buildSQL", new Class[]{});
        m.setAccessible(true);

        String result = (String) m.invoke(cdcErrorRecoverExecutor, null);

        Assert.assertNotNull(result);
        Assert.assertEquals(expectedRecover, result);
    }

    @Test
    public void fullQuerySqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        checkByCondition(true, expectedFullSelect);
    }

    @Test
    public void expectedFullNotEqualStatusSelectSqlTest() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        checkByCondition(false, expectedFullNotEqualStatusSelect);
    }

    @Test
    public void QueryIdConditionSqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        CdcErrorQueryCondition expectErrorQueryCondition = new CdcErrorQueryCondition();
        expectErrorQueryCondition.setId(1L);

        CdcErrorQueryExecutor cdcErrorQueryExecutor = new CdcErrorQueryExecutor(tableName, null, 0);
        Method m = cdcErrorQueryExecutor.getClass()
                .getDeclaredMethod("buildSQL", new Class[]{StringBuilder.class, CdcErrorQueryCondition.class});
        m.setAccessible(true);

        StringBuilder stringBuilder = new StringBuilder();
        Boolean result = (Boolean) m.invoke(cdcErrorQueryExecutor, stringBuilder, expectErrorQueryCondition);

        Assert.assertNotNull(result);
        Assert.assertTrue(result);

        Assert.assertEquals(expectedIdSelect, stringBuilder.toString());
    }

    @Test
    public void EmptyQuerySqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        CdcErrorQueryCondition expectErrorQueryCondition = new CdcErrorQueryCondition();

        CdcErrorQueryExecutor cdcErrorQueryExecutor = new CdcErrorQueryExecutor(tableName, null, 0);
        Method m = cdcErrorQueryExecutor.getClass()
                .getDeclaredMethod("buildSQL", new Class[]{StringBuilder.class, CdcErrorQueryCondition.class});
        m.setAccessible(true);

        StringBuilder stringBuilder = new StringBuilder();
        Boolean result = (Boolean) m.invoke(cdcErrorQueryExecutor, stringBuilder, expectErrorQueryCondition);

        Assert.assertNotNull(result);
        Assert.assertFalse(result);

        Assert.assertEquals(expectedEmptySelect, stringBuilder.toString());
    }

    private void checkByCondition(boolean isEquals, String expectString) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CdcErrorQueryCondition expectErrorQueryCondition = init(isEquals);

        CdcErrorQueryExecutor cdcErrorQueryExecutor = new CdcErrorQueryExecutor(tableName, null, 0);
        Method m = cdcErrorQueryExecutor.getClass()
                .getDeclaredMethod("buildSQL", new Class[]{StringBuilder.class, CdcErrorQueryCondition.class});
        m.setAccessible(true);

        StringBuilder stringBuilder = new StringBuilder();
        Boolean result = (Boolean) m.invoke(cdcErrorQueryExecutor, stringBuilder, expectErrorQueryCondition);

        Assert.assertNotNull(result);
        Assert.assertTrue(result);

        Assert.assertEquals(expectString, stringBuilder.toString());
    }

    private CdcErrorQueryCondition init(boolean isEquals) {
        CdcErrorQueryCondition expectErrorQueryCondition = new CdcErrorQueryCondition();
        expectErrorQueryCondition.setSeqNo(1L);
        expectErrorQueryCondition.setBatchId(1L);
        expectErrorQueryCondition.setId(Long.MAX_VALUE);
        expectErrorQueryCondition.setCommitId(2L);
        expectErrorQueryCondition.setRangeLEExecuteTime(99L);
        expectErrorQueryCondition.setRangeGeExecuteTime(1L);
        expectErrorQueryCondition.setRangeLEFixedTime(200L);
        expectErrorQueryCondition.setRangeGeFixedTime(100L);
        expectErrorQueryCondition.setType(ErrorType.DATA_FORMAT_ERROR.ordinal());
        expectErrorQueryCondition.setStatus(FixedStatus.FIXED.ordinal());
        if (!isEquals) {
            expectErrorQueryCondition.setEqualStatus(false);
        }

        return expectErrorQueryCondition;
    }
}
