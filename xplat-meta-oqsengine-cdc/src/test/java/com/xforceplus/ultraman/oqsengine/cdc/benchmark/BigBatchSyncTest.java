package com.xforceplus.ultraman.oqsengine.cdc.benchmark;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.connect.SingleCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerRunner;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceNoShardResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.AbstractMasterExecutor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.shaded.org.apache.commons.lang.time.StopWatch;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar.*;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

/**
 * desc :
 * name : BigBatchSyncTest
 *
 * @author : xujia
 * date : 2020/11/23
 * @since : 1.8
 */
public class BigBatchSyncTest extends AbstractContainer {
    final Logger logger = LoggerFactory.getLogger(BigBatchSyncTest.class);

    private static int expectedSize = 0;
    private static int maxTestSize = 10;

    private ConsumerRunner consumerRunner;

    private MockRedisCallbackService mockRedisCallbackService;

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

        SingleCDCConnector singleCDCConnector = new SingleCDCConnector();
        singleCDCConnector.init(System.getProperty("CANAL_HOST"), Integer.parseInt(System.getProperty("CANAL_PORT")),
                "nly-v1", "root", "xplat");

        return new ConsumerRunner(initAll(), cdcMetricsService, singleCDCConnector);
    }

    @Test
    public void test() throws InterruptedException, SQLException {
        initData();
        StopWatch stopWatch = new StopWatch();

        boolean isStartUpdate = false;
        while (true) {
            if (!isStartUpdate && mockRedisCallbackService.getExecuted().get() > ZERO) {
                stopWatch.start();
                isStartUpdate = true;
            }
            if (mockRedisCallbackService.getExecuted().get() < expectedSize) {
                Thread.sleep(1_000);
            } else {
                stopWatch.stop();
                break;
            }
        }

        Assert.assertEquals(expectedSize, mockRedisCallbackService.getExecuted().get());
        logger.info("total build use time, {}", stopWatch.getTime());

        mockRedisCallbackService.reset();
        Thread.sleep(5_000);
        Assert.assertEquals(ZERO, mockRedisCallbackService.getExecuted().get());

        //  额外测试单条数据不存在父类的情况,需要从主库查询
        IEntity entity = testNoPref(333L);
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        try {
            build(entity);
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
        tx.commit();
        transactionManager.finish();

        while (true) {
            if (mockRedisCallbackService.getExecuted().get() == 1) {
                break;
            }
        }
        Assert.assertTrue(mockRedisCallbackService.getExecuted().get() > 0);
    }

    private IEntity testNoPref(long id) {
        IEntityValue values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 1L), new StringValue(stringField, "v1"),
                new BooleanValue(boolField, true),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 1, 1, 0, 0, 1)),
                new DecimalValue(decimalField, new BigDecimal("0.0")), new EnumValue(enumField, "1"),
                new StringsValue(stringsField, "value1", "value2")));
        return new Entity(id, entityClass, values, new EntityFamily(2, 0), 0, OqsVersion.MAJOR);
    }

    private void initData() throws SQLException {
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        try {
            int i = 1;
            for (; i < maxTestSize; ) {
                IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(i, 0);
                for (IEntity entity : entities) {
                    build(entity);
                }
                expectedSize += entities.length;
                i += entities.length;
            }
            tx.commit();
            transactionManager.finish();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    private int build(IEntity entity) throws SQLException {
        return masterStorage.build(entity);
    }

    private int replace(long commitId) throws SQLException {
        return (Integer) masterTransactionExecutor.execute(
                new DataSourceNoShardResourceTask(dataSource) {

                    @Override
                    public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                        BigBatchSyncExecutor bigBatchSyncExecutor = new BigBatchSyncExecutor(tableName, resource, 3000);
                        return bigBatchSyncExecutor.execute(commitId);
                    }
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