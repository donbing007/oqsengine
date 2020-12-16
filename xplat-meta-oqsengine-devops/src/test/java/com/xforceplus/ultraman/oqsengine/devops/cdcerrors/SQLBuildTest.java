package com.xforceplus.ultraman.oqsengine.devops.cdcerrors;

import com.xforceplus.ultraman.oqsengine.devops.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.devops.cdcerror.executor.impl.CdcErrorBuildExecutor;
import com.xforceplus.ultraman.oqsengine.devops.cdcerror.executor.impl.CdcErrorQueryExecutor;
import com.xforceplus.ultraman.oqsengine.devops.cdcerror.executor.impl.CdcErrorUpdateExecutor;
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

    private String expectedBuild = "INSERT INTO cdcerrors (seqno,id,commitid,status,message,executetime,fixedtime) VALUES (?,?,?,?,?,?,?)";
    private String expectedUpdate = "UPDATE cdcerrors SET status=?, fixedtime=? WHERE seqno=?";
    private String expectedFullSelect = "SELECT seqno,id,commitid,status,message,executetime,fixedtime FROM cdcerrors WHERE seqno=? AND id=? AND commitid=? AND status=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=?";
    private String expectedEmptySelect = "SELECT seqno,id,commitid,status,message,executetime,fixedtime FROM cdcerrors";
    private String expectedIdSelect = "SELECT seqno,id,commitid,status,message,executetime,fixedtime FROM cdcerrors WHERE id=?";
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
    public void FullQuerySqlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        CdcErrorQueryCondition expectErrorQueryCondition = init();

        CdcErrorQueryExecutor cdcErrorQueryExecutor = new CdcErrorQueryExecutor(tableName, null, 0);
        Method m = cdcErrorQueryExecutor.getClass()
                .getDeclaredMethod("buildSQL", new Class[]{StringBuilder.class, CdcErrorQueryCondition.class});
        m.setAccessible(true);

        StringBuilder stringBuilder = new StringBuilder();
        Boolean result = (Boolean) m.invoke(cdcErrorQueryExecutor, stringBuilder, expectErrorQueryCondition);

        Assert.assertNotNull(result);
        Assert.assertTrue(result);

        Assert.assertEquals(expectedFullSelect, stringBuilder.toString());
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

    private CdcErrorQueryCondition init() {
        CdcErrorQueryCondition expectErrorQueryCondition = new CdcErrorQueryCondition();
        expectErrorQueryCondition.setSeqNo(1L);
        expectErrorQueryCondition.setId(Long.MAX_VALUE);
        expectErrorQueryCondition.setCommitId(2L);
        expectErrorQueryCondition.setRangeLEExecuteTime(99L);
        expectErrorQueryCondition.setRangeGeExecuteTime(1L);
        expectErrorQueryCondition.setRangeLEFixedTime(200L);
        expectErrorQueryCondition.setRangeGeFixedTime(100L);
        expectErrorQueryCondition.setStatus(FixedStatus.FIXED.ordinal());

        return expectErrorQueryCondition;
    }
}
