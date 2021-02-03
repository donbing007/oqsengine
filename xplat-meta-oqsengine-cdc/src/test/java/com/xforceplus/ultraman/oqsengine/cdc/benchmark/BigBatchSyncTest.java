package com.xforceplus.ultraman.oqsengine.cdc.benchmark;

import com.xforceplus.ultraman.oqsengine.cdc.CDCAbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerRunner;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.AbstractMasterExecutor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

/**
 * desc :
 * name : BigBatchSyncTest
 *
 * @author : xujia
 * date : 2020/11/23
 * @since : 1.8
 */
public class BigBatchSyncTest extends CDCAbstractContainer {
    final Logger logger = LoggerFactory.getLogger(BigBatchSyncTest.class);

    private static int expectedSize = 0;
    private static int maxTestSize = 10;

    private ConsumerRunner consumerRunner;

    private MockRedisCallbackService mockRedisCallbackService;

    @BeforeClass
    public static void beforeClass() {
        ContainerStarter.startMysql();
        ContainerStarter.startManticore();
        ContainerStarter.startRedis();
        ContainerStarter.startCannal();
    }

    @AfterClass
    public static void afterClass() {
        ContainerStarter.reset();
    }

    @Before
    public void before() throws Exception {
        consumerRunner = initConsumerRunner();
        consumerRunner.start();
    }

    @After
    public void after() throws SQLException {
        consumerRunner.shutdown();
        clear();
        closeAll();
    }

    private ConsumerRunner initConsumerRunner() throws Exception {
        CDCMetricsService cdcMetricsService = new CDCMetricsService();
        mockRedisCallbackService = new MockRedisCallbackService();
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

        return new ConsumerRunner(initAll(), cdcMetricsService, singleCDCConnector);
    }

    @Test
    public void test() throws InterruptedException, SQLException {
        initData();

        boolean isStartUpdate = false;
        long start = 0;
        long duration = 0;
        while (true) {
            if (!isStartUpdate && mockRedisCallbackService.getExecuted().get() > ZERO) {
                start = System.currentTimeMillis();
                isStartUpdate = true;
            }
            if (mockRedisCallbackService.getExecuted().get() < expectedSize) {
                Thread.sleep(1_000);
            } else {
                duration = System.currentTimeMillis() - start;
                break;
            }
        }

        Assert.assertEquals(expectedSize, mockRedisCallbackService.getExecuted().get());
        logger.info("total build use time, {}", duration);

        mockRedisCallbackService.reset();
        Thread.sleep(5_000);
        Assert.assertEquals(ZERO, mockRedisCallbackService.getExecuted().get());

//        //  额外测试单条数据不存在父类的情况,需要从主库查询
//        IEntity entity = testNoPref(333L);
//        Transaction tx = transactionManager.create();
//        transactionManager.bind(tx.id());
//        try {
//            build(entity);
//        } catch (Exception e) {
//            tx.rollback();
//            throw e;
//        }
//        tx.commit();
//        transactionManager.finish();
//
//        while (true) {
//            if (mockRedisCallbackService.getExecuted().get() == 1) {
//                break;
//            }
//        }
//        Assert.assertTrue(mockRedisCallbackService.getExecuted().get() > 0);
    }
//
//    private IEntity testNoPref(long id) {
//        IEntityValue values = new EntityValue(id);
//        values.addValues(Arrays.asList(new LongValue(longField, 1L), new StringValue(stringField, "v1"),
//                new BooleanValue(boolField, true),
//                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 1, 1, 0, 0, 1)),
//                new DecimalValue(decimalField, new BigDecimal("0.0")), new EnumValue(enumField, "1"),
//                new StringsValue(stringsField, "value1", "value2")));
//        return new Entity(id, entityClass, values, new EntityFamily(2, 0), 0, 0);
//    }

    private void initData() throws SQLException {
//        Transaction tx = transactionManager.create();
//        transactionManager.bind(tx.id());
        try {
            int i = 1;
            for (; i < maxTestSize; ) {
                IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(i, 0);
                for (IEntity entity : entities) {
                    masterStorage.build(entity);
                }
                expectedSize += entities.length;
                i += entities.length;
            }
//            tx.commit();
        } catch (Exception e) {
//            tx.rollback();
            throw e;
        } finally {
//            transactionManager.finish();
        }
    }


    private int replace(long commitId) throws SQLException {
        return (Integer) masterTransactionExecutor.execute(
            (resource, hint) -> {
                BigBatchSyncExecutor bigBatchSyncExecutor = new BigBatchSyncExecutor(tableName, resource, 3000);
                return bigBatchSyncExecutor.execute(commitId);
            });
    }

    private class BigBatchSyncExecutor extends AbstractMasterExecutor<Long, Integer> {

        public BigBatchSyncExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
            super(tableName, resource, timeoutMs);
        }

        @Override
        public Integer execute(Long commitId) throws SQLException {
            String sql = buildSQL();
            PreparedStatement st = getResource().value().prepareStatement(sql);
            st.setLong(1, commitId);

            checkTimeout(st);

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            try {
                return st.executeUpdate();
            } finally {
                if (st != null) {
                    st.close();
                }
            }
        }

        private String buildSQL() {
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(getTableName())
                .append(" SET ")
                .append(FieldDefine.COMMITID).append("=").append("?");
            return sql.toString();
        }
    }
}
