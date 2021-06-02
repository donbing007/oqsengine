package com.xforceplus.ultraman.oqsengine.storage.master.executor.errors;


import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.condition.QueryErrorCondition;
import com.xforceplus.ultraman.oqsengine.storage.master.define.ErrorDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.SqlConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import com.zaxxer.hikari.HikariDataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import java.sql.Statement;
import java.util.Collection;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.MYSQL})
public class ErrorExecutorTest {

    private TransactionManager transactionManager;

    private DataSource dataSource;
    private SQLMasterStorage storage;

    static String[] expectedSql = new String[2];

    static {
        expectedSql[0] = String.format(
            "SELECT %s,%s,%s,%s,%s,%s FROM entityfaileds WHERE %s=? AND %s=? AND %s=? AND %s>? AND %s<? ORDER BY %s DESC LIMIT ?,?",
            ErrorDefine.ID, ErrorDefine.ENTITY, ErrorDefine.ERRORS, ErrorDefine.EXECUTE_TIME, ErrorDefine.FIXED_TIME,
            ErrorDefine.STATUS,
            ErrorDefine.ID, ErrorDefine.ENTITY, ErrorDefine.STATUS, ErrorDefine.EXECUTE_TIME, ErrorDefine.EXECUTE_TIME,
            ErrorDefine.EXECUTE_TIME
        );

        expectedSql[1] = "REPLACE INTO entityfaileds VALUES (?,?,?,?,?,?)";
    }

    @Before
    public void before() throws SQLException {

        transactionManager = DefaultTransactionManager.Builder.anDefaultTransactionManager()
            .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCacheEventHandler(new DoNothingCacheEventHandler())
            .withWaitCommitSync(false)
            .build();

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        storageStrategyFactory.register(FieldType.STRINGS, new MasterStringsStorageStrategy());

        SQLJsonConditionsBuilderFactory sqlJsonConditionsBuilderFactory = new SQLJsonConditionsBuilderFactory();
        sqlJsonConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sqlJsonConditionsBuilderFactory.init();

        storage = new SQLMasterStorage();

        DataSource ds = buildDataSource("./src/test/resources/sql_master_storage_build.conf");
        TransactionExecutor executor = new AutoJoinTransactionExecutor(
            transactionManager, new SqlConnectionTransactionResourceFactory("entityfaileds"),
            new NoSelector<>(ds), new NoSelector<>("entityfaileds"));

        ReflectionTestUtils.setField(storage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(storage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(storage, "conditionsBuilderFactory", sqlJsonConditionsBuilderFactory);
        ReflectionTestUtils.setField(storage, "asyncErrorExecutor", Executors.newFixedThreadPool(2));

        storage.setErrorTable("entityfaileds");
        storage.setQueryTimeout(100000000);
        storage.init();
    }

    @After
    public void after() throws Exception {

        transactionManager.finish();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stat = conn.createStatement()) {
                stat.execute("truncate table entityfaileds");
            }
        }

        ((HikariDataSource) dataSource).close();

    }

    @Test
    public void testQuerySqlParser() throws Exception {
        QueryErrorExecutor queryErrorExecutor = new QueryErrorExecutor("entityfaileds", null, 3600);

        QueryErrorCondition expectedQueryErrorCondition = initFullQueryErrorCondition();

        Method m = QueryErrorExecutor.class
            .getDeclaredMethod("buildSQL", QueryErrorCondition.class);
        m.setAccessible(true);

        String result = (String) m.invoke(queryErrorExecutor, expectedQueryErrorCondition);

        Assert.assertEquals(expectedSql[0], result);
    }


    @Test
    public void testReplaceSqlParser() throws Exception {
        ReplaceErrorExecutor replaceErrorExecutor = new ReplaceErrorExecutor("entityfaileds", null);

        Method m = ReplaceErrorExecutor.class
            .getDeclaredMethod("buildSQL");
        m.setAccessible(true);

        String result = (String) m.invoke(replaceErrorExecutor);

        Assert.assertEquals(expectedSql[1], result);
    }

    @Test
    public void testReplaceQuery() throws SQLException, InterruptedException {
        QueryErrorCondition errorCondition = initFullQueryErrorCondition();
        Collection<ErrorStorageEntity> selectErrors = storage.selectErrors(errorCondition);
        Assert.assertTrue(selectErrors.isEmpty());

        Thread.sleep(1_000);
        //  将entityId设置为maintainId
        ErrorStorageEntity errorStorageEntity = ErrorStorageEntity.Builder.anErrorStorageEntity()
            .withMaintainId(errorCondition.getMaintainId())
            .withEntity(errorCondition.getEntity())
            .withId(errorCondition.getId())
            .withErrors("test error")
            .withFixedStatus(errorCondition.getFixedStatus().getStatus())
            .build();

        storage.writeError(errorStorageEntity);

        Thread.sleep(5_000);

        selectErrors = storage.selectErrors(errorCondition);
        Assert.assertEquals(1, selectErrors.size());

        selectErrors.forEach(
            error -> {
                Assert.assertEquals(errorCondition.getMaintainId().longValue(), error.getMaintainId());
                Assert.assertEquals(errorCondition.getId().longValue(), error.getId());
                Assert.assertEquals(errorCondition.getEntity().longValue(), error.getEntity());
                Assert.assertEquals(errorCondition.getFixedStatus().getStatus(), error.getStatus());
                Assert.assertEquals("test error", error.getErrors());
            }
        );

        //  当设置时间范围不对时，应不存在记录
        errorCondition.setEndTime(errorCondition.getStartTime() + 1000L);
        selectErrors = storage.selectErrors(errorCondition);
        Assert.assertTrue(selectErrors.isEmpty());
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

    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        DataSourcePackage dataSourcePackage = DataSourceFactory.build(true);
        this.dataSource = dataSourcePackage.getMaster().get(0);
        return dataSource;
    }
}
