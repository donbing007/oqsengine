package com.xforceplus.ultraman.oqsengine.storage.master.executor.errors;


import static com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization.MASTER_STORAGE_FAILED_TABLE;


import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.condition.QueryErrorCondition;
import com.xforceplus.ultraman.oqsengine.storage.master.define.ErrorDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import java.lang.reflect.Method;

import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */

public class ErrorExecutorTest extends AbstractContainerExtends {

    static String[] expectedSql = new String[2];

    static {
        expectedSql[0] = String.format(
            "SELECT %s,%s,%s,%s,%s,%s FROM entityfaileds WHERE %s=? AND %s=? AND %s=? AND %s>? AND %s<? ORDER BY %s DESC LIMIT ?,?",
            ErrorDefine.ID, ErrorDefine.ENTITY, ErrorDefine.ERRORS, ErrorDefine.EXECUTE_TIME, ErrorDefine.FIXED_TIME,
            ErrorDefine.STATUS,
            ErrorDefine.ID, ErrorDefine.ENTITY, ErrorDefine.STATUS, ErrorDefine.EXECUTE_TIME, ErrorDefine.EXECUTE_TIME,
            ErrorDefine.EXECUTE_TIME
        );

        expectedSql[1] = "REPLACE INTO " + MASTER_STORAGE_FAILED_TABLE + " VALUES (?,?,?,?,?,?)";
    }

    @BeforeEach
    public void before() throws Exception {
        MasterDBInitialization.getInstance().resetTransactionExecutor(MASTER_STORAGE_FAILED_TABLE);
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
    }

    @Test
    public void testQuerySqlParser() throws Exception {

        QueryErrorExecutor queryErrorExecutor = new QueryErrorExecutor(MASTER_STORAGE_FAILED_TABLE, null, 3600);

        QueryErrorCondition expectedQueryErrorCondition = initFullQueryErrorCondition();

        Method m = QueryErrorExecutor.class
            .getDeclaredMethod("buildSQL", QueryErrorCondition.class);
        m.setAccessible(true);

        String result = (String) m.invoke(queryErrorExecutor, expectedQueryErrorCondition);

        Assertions.assertEquals(expectedSql[0], result);
    }

    @Test
    public void testReplaceSqlParser() throws Exception {
        ReplaceErrorExecutor replaceErrorExecutor = new ReplaceErrorExecutor(MASTER_STORAGE_FAILED_TABLE, null);

        Method m = ReplaceErrorExecutor.class
            .getDeclaredMethod("buildSQL");
        m.setAccessible(true);

        String result = (String) m.invoke(replaceErrorExecutor);

        Assertions.assertEquals(expectedSql[1], result);
    }

    @Test
    public void testReplaceQuery() throws Exception {
        SQLMasterStorage masterStorage = MasterDBInitialization.getInstance().getMasterStorage();

        QueryErrorCondition errorCondition = initFullQueryErrorCondition();
        Collection<ErrorStorageEntity> selectErrors = masterStorage.selectErrors(errorCondition);
        Assertions.assertTrue(selectErrors.isEmpty());

        Thread.sleep(1_000);
        //  将entityId设置为maintainId
        ErrorStorageEntity errorStorageEntity = ErrorStorageEntity.Builder.anErrorStorageEntity()
            .withMaintainId(errorCondition.getMaintainId())
            .withEntity(errorCondition.getEntity())
            .withId(errorCondition.getId())
            .withErrors("test error")
            .withFixedStatus(errorCondition.getFixedStatus().getStatus())
            .build();

        masterStorage.writeError(errorStorageEntity);

        Thread.sleep(5_000);

        selectErrors = masterStorage.selectErrors(errorCondition);
        Assertions.assertEquals(1, selectErrors.size());

        selectErrors.forEach(
            error -> {
                Assertions.assertEquals(errorCondition.getMaintainId().longValue(), error.getMaintainId());
                Assertions.assertEquals(errorCondition.getId().longValue(), error.getId());
                Assertions.assertEquals(errorCondition.getEntity().longValue(), error.getEntity());
                Assertions.assertEquals(errorCondition.getFixedStatus().getStatus(), error.getStatus());
                Assertions.assertEquals("test error", error.getErrors());
            }
        );

        //  当设置时间范围不对时，应不存在记录
        errorCondition.setEndTime(errorCondition.getStartTime() + 1000L);
        selectErrors = MasterDBInitialization.getInstance().getMasterStorage().selectErrors(errorCondition);
        Assertions.assertTrue(selectErrors.isEmpty());
    }

    private QueryErrorCondition initFullQueryErrorCondition() {
        QueryErrorCondition queryErrorCondition = new QueryErrorCondition();
        queryErrorCondition.setMaintainId(1L);
        queryErrorCondition.setId(1L);
        queryErrorCondition.setEntity(2L);
        queryErrorCondition.setFixedStatus(FixedStatus.NOT_FIXED);
        queryErrorCondition.setStartTime(System.currentTimeMillis() - 1000L);
        queryErrorCondition.setEndTime(System.currentTimeMillis() + 5000L);
        queryErrorCondition.setStartPos(0L);
        queryErrorCondition.setSize(256);
        return queryErrorCondition;
    }
}
