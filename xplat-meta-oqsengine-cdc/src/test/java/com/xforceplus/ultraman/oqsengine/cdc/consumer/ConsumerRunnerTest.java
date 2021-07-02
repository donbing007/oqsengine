package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.getEntityClass;

import com.xforceplus.ultraman.oqsengine.cdc.CDCTestHelper;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * desc :.
 * name : ConsumerRunnerTest
 *
 * @author : xujia 2020/11/9
 * @since : 1.8
 */
public class ConsumerRunnerTest extends CDCTestHelper {
    final Logger logger = LoggerFactory.getLogger(ConsumerRunnerTest.class);

    private long startId = 0;

    private int expectedCount = 0;

    @BeforeEach
    public void before() throws Exception {
        super.init(true);

    }

    @AfterEach
    public void after() throws Exception {
        super.destroy(true);
    }

    private void startConsumerRunner(long partitionId) {
        startId = partitionId;
        expectedCount = 0;
    }

    private void stopConsumerRunner(String func) throws InterruptedException {
        int loop = 0;
        int maxLoop = 100;
        while (loop < maxLoop) {
            if (expectedCount == mockRedisCallbackService.getExecuted().get()) {
                break;
            }
            logger.warn("func -> {}, current -> {}, expectedCount -> {}", func,
                mockRedisCallbackService.getExecuted().get(), expectedCount);

            Thread.sleep(1_000);
            loop++;
        }
        logger.debug("result loop : {}, expectedCount : {}, actual : {}", loop, expectedCount,
            mockRedisCallbackService.getExecuted().get());
        Assertions.assertEquals(expectedCount, mockRedisCallbackService.getExecuted().get());
    }


    @Test
    public void syncTest() throws Exception {

        startConsumerRunner(1);
        try {
            TransactionManager transactionManager = StorageInitialization.getInstance().getTransactionManager();
            Transaction tx = transactionManager.create(30_000);
            transactionManager.bind(tx.id());

            try {
                IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(startId, 0);
                initData(tx, entities, false, false);

                Thread.sleep(1000);

                entities = EntityGenerateToolBar.generateFixedEntities(startId, 1);
                initData(tx, entities, true, false);

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
    public void syncDeleteTest() throws Exception {
        startConsumerRunner(1000000);

        try {
            TransactionManager transactionManager = StorageInitialization.getInstance().getTransactionManager();
            Transaction tx = transactionManager.create(30_000);
            transactionManager.bind(tx.id());

            try {
                IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(startId, 0);
                initData(tx, entities, false, false);

                Thread.sleep(1000);

                initData(tx, entities, true, true);
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
                TransactionManager transactionManager = StorageInitialization.getInstance().getTransactionManager();
                Transaction tx = transactionManager.create(30_000);
                transactionManager.bind(tx.id());
                try {
                    IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(startId, 0);
                    initData(tx, entities, false, false);

                    entities = EntityGenerateToolBar.generateFixedEntities(startId, 1);
                    initData(tx, entities, true, false);
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
                startId += gap;
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
            TransactionManager transactionManager = StorageInitialization.getInstance().getTransactionManager();
            Transaction tx = transactionManager.create(30_000);
            transactionManager.bind(tx.id());

            try {
                IEntity[] entities;
                for (long i = startId; i < startId + gap * size; i += gap) {
                    entities = EntityGenerateToolBar.generateFixedEntities(i, 0);
                    initData(tx, entities, false, false);
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
            long i = startId;
            long limits = startId + gap * loops;

            while (i < limits) {
                TransactionManager transactionManager = StorageInitialization.getInstance().getTransactionManager();
                Transaction tx = transactionManager.create();
                transactionManager.bind(tx.id());
                try {
                    IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(i, 0);
                    initData(tx, entities, false, false);
                    initData(tx, EntityGenerateToolBar.generateFixedEntities(i, 1), true, false);

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

    private void initData(Transaction tx, IEntity[] datas, boolean replacement, boolean delete) throws Exception {
        for (IEntity entity : datas) {
            if (delete) {
                MasterDBInitialization
                    .getInstance().getMasterStorage().delete(entity, getEntityClass(entity.entityClassRef().getId()));
                tx.getAccumulator().accumulateDelete(entity);
            } else if (replacement) {
                entity.resetVersion(0);
                MasterDBInitialization.getInstance().getMasterStorage().replace(entity, getEntityClass(entity.entityClassRef().getId()));
                tx.getAccumulator().accumulateReplace(entity, entity);
            } else {
                MasterDBInitialization.getInstance().getMasterStorage().build(entity, getEntityClass(entity.entityClassRef().getId()));
                tx.getAccumulator().accumulateBuild(entity);
            }
        }
    }
}
