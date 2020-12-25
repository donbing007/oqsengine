package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.xforceplus.ultraman.oqsengine.cdc.CDCAbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;


/**
 * desc :
 * name : ConsumerRunnerTest
 *
 * @author : xujia
 * date : 2020/11/9
 * @since : 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS, ContainerType.MYSQL, ContainerType.MANTICORE, ContainerType.CANNAL})
public class ConsumerRunnerTest extends CDCAbstractContainer {
    final Logger logger = LoggerFactory.getLogger(ConsumerRunnerTest.class);
    private ConsumerRunner consumerRunner;

    private MockRedisCallbackService mockRedisCallbackService;

    private long t = 0;

    private int expectedCount = 0;

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

    private void startConsumerRunner(long partitionId) throws Exception {
        t = partitionId;
        expectedCount = 0;
    }

    private void stopConsumerRunner(String func) throws InterruptedException {
        int loop = 0;
        int maxLoop = 100;
        while (loop < maxLoop) {
            if (expectedCount == mockRedisCallbackService.getExecuted().get()) {
                break;
            }
            logger.warn("func -> {}, current -> {}, expectedCount -> {}", func, mockRedisCallbackService.getExecuted().get(), expectedCount);

            Thread.sleep(1_000);
            loop++;
        }
        logger.debug("result loop : {}, expectedCount : {}, actual : {}", loop, expectedCount, mockRedisCallbackService.getExecuted().get());
        Assert.assertEquals(expectedCount, mockRedisCallbackService.getExecuted().get());
    }

    @Test
    public void syncTest() throws Exception {

        startConsumerRunner(1);
        try {
            Transaction tx = transactionManager.create(30_000);
            transactionManager.bind(tx.id());

            try {
                IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(t, 0);
                initData(entities, false, false);

                Thread.sleep(1000);

                entities = EntityGenerateToolBar.generateFixedEntities(t, 1);
                initData(entities, true, false);

                expectedCount += entities.length;

            } catch (Exception ex) {
                tx.rollback();
                throw ex;
            }

            //将事务正常提交,并从事务管理器中销毁事务.
            tx.commit();
            transactionManager.finish();
        } finally {
            stopConsumerRunner("syncTest");
        }
    }

    @Test
    public void SyncDeleteTest() throws Exception {
        startConsumerRunner(1000000);

        try {
            Transaction tx = transactionManager.create(30_000);
            transactionManager.bind(tx.id());

            try {
                IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(t, 0);
                initData(entities, false, false);

                Thread.sleep(1000);

                initData(entities, true, true);
                expectedCount += entities.length;
            } catch (Exception ex) {
                tx.rollback();
                throw ex;
            }

            //将事务正常提交,并从事务管理器中销毁事务.
            tx.commit();
            transactionManager.finish();
        } finally {
            stopConsumerRunner("SyncDeleteTest");
        }
    }

    @Test
    public void loopTest() throws Exception {
        int gap = 10;
        long loops = 2;
        int i = 0;

        startConsumerRunner(50);

        try {
            while (i < loops) {
                Transaction tx = transactionManager.create(30_000);
                transactionManager.bind(tx.id());
                try {
                    IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(t, 0);
                    initData(entities, false, false);

                    entities = EntityGenerateToolBar.generateFixedEntities(t, 1);
                    initData(entities, true, false);
                    expectedCount += entities.length;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.error("loop test write data error, ex : {}", ex.getMessage());
                    tx.rollback();
                    throw ex;
                }

                //将事务正常提交,并从事务管理器中销毁事务.
                tx.commit();
                transactionManager.finish();

                i++;
                t += gap;
            }
        } finally {
            stopConsumerRunner("loopTest");
        }
    }

    @Test
    public void loopTransactionOverBatches() throws Exception {
        int gap = 10;
        int size = 100;

        startConsumerRunner(15000);
        try {
            Transaction tx = transactionManager.create(30_000);
            transactionManager.bind(tx.id());

            try {
                IEntity[] entities;
                for (long i = t; i < t + gap * size; i += gap) {
                    entities = EntityGenerateToolBar.generateFixedEntities(i, 0);
                    initData(entities, false, false);
                    expectedCount += entities.length;
                }

            } catch (Exception ex) {
                tx.rollback();
                throw ex;
            }

            tx.commit();
            transactionManager.finish();
        } finally {
            stopConsumerRunner("loopTransactionOverBatches");
        }
    }


    @Test
    public void loopSmallTransactionBatches() throws Exception {
        int gap = 10;
        int loops = 100;

        startConsumerRunner(50000);
        try {
            long i = t;
            long limits = t + gap * loops;

            while (i < limits) {
                Transaction tx = transactionManager.create();
                transactionManager.bind(tx.id());
                try {
                    IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(i, 0);
                    initData(entities, false, false);
                    initData(EntityGenerateToolBar.generateFixedEntities(i, 1), true, false);

                    expectedCount += entities.length;
                    tx.commit();
                } catch (Exception ex) {
                    tx.rollback();
                    throw ex;
                } finally {
                    transactionManager.finish();
                }
                i += gap;
            }
        } finally {
            stopConsumerRunner("loopSmallTransactionBatches");
        }
    }


    private void initData(IEntity[] datas, boolean replacement, boolean delete) throws SQLException {
        for (IEntity entity : datas) {
            if (delete) {
                masterStorage.delete(entity);
            } else if (replacement) {
                entity.resetVersion(0);
                masterStorage.replace(entity);
            } else {
                masterStorage.build(entity);
            }
        }
    }
}
