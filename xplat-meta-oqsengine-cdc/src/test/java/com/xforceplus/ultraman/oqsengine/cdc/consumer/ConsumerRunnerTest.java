package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.connect.SingleCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceNoShardStorageTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.BuildExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.ReplaceExecutor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Optional;


/**
 * desc :
 * name : ConsumerRunnerTest
 *
 * @author : xujia
 * date : 2020/11/9
 * @since : 1.8
 */
public class ConsumerRunnerTest extends AbstractContainer {

    private ConsumerRunner consumerRunner;

    private MockRedisCallbackService mockRedisCallbackService;

    @Before
    public void before() throws Exception {
        initMaster();

        initConsumerRunner();
    }

    private void initConsumerRunner() throws SQLException, InterruptedException {
        CDCMetricsService cdcMetricsService = new CDCMetricsService();
        mockRedisCallbackService = new MockRedisCallbackService();
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

        SingleCDCConnector singleCDCConnector = new SingleCDCConnector();
        singleCDCConnector.init("localhost",
                environment.getServicePort("canal-server_1", 11111),
                "nly-v1", "root", "xplat");

        consumerRunner = new ConsumerRunner(initConsumerService(), cdcMetricsService, singleCDCConnector);
        consumerRunner.start();
    }

    private long t = 1;

    @Test
    public void syncTest() throws InterruptedException, SQLException {

        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        int expectedCount = 0;
        try {
            initData(EntityGenerateToolBar.generateFixedEntities(t, 0), false);

            Thread.sleep(1000);

            IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(t, 1);
            initData(entities, true);
            expectedCount = entities.length;

            //将事务正常提交,并从事务管理器中销毁事务.
            tx.commit();
            transactionManager.finish();
        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }

        Thread.sleep(1000 * 1000);

        CDCMetrics cdcMetrics = mockRedisCallbackService.queryLastUnCommit();
        Assert.assertNotNull(cdcMetrics);
        Assert.assertNotNull(cdcMetrics.getCdcAckMetrics());

        Assert.assertNotNull(cdcMetrics.getCdcUnCommitMetrics());
        Assert.assertEquals(expectedCount, cdcMetrics.getCdcUnCommitMetrics().getExecuteJobCount());
    }

    @Test
    public void loopTest() throws InterruptedException, SQLException {
        t = 50;
        int gap = 10;
        long loops = 2;
        int i = 0;
        mockRedisCallbackService.reset();
        while (i < loops) {
            Transaction tx = transactionManager.create();
            transactionManager.bind(tx.id());
            try {
                initData(EntityGenerateToolBar.generateFixedEntities(t, 0), false);

                Thread.sleep(1000);

                initData(EntityGenerateToolBar.generateFixedEntities(t, 1), true);

            } catch (Exception ex) {
                tx.rollback();
                throw ex;
            }

            //将事务正常提交,并从事务管理器中销毁事务.
            tx.commit();
            transactionManager.finish();

            i++;
            t += gap;
        }

        Thread.sleep(10000);

        CDCMetrics cdcMetrics = mockRedisCallbackService.queryLastUnCommit();
        Assert.assertNotNull(cdcMetrics);
        Assert.assertNotNull(cdcMetrics.getCdcAckMetrics());

        Assert.assertNotNull(cdcMetrics.getCdcUnCommitMetrics());
    }

    @Test
    public void loopTransactionOverBatches() throws SQLException, InterruptedException {
        t = 15000;
        int gap = 10;
        int size = 250;
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        mockRedisCallbackService.reset();
        try {
            for (long i = t; i < t + gap * size; i += gap) {
                initData(EntityGenerateToolBar.generateFixedEntities(i, 0), false);
            }

            for (long i = t; i < t + gap * size; i += gap) {
                initData(EntityGenerateToolBar.generateFixedEntities(i, 1), true);
            }
        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }

        tx.commit();
        transactionManager.finish();

        Thread.sleep(30000);

        CDCMetrics cdcMetrics = mockRedisCallbackService.queryLastUnCommit();
        Assert.assertNotNull(cdcMetrics);
        Assert.assertNotNull(cdcMetrics.getCdcAckMetrics());

        Assert.assertNotNull(cdcMetrics.getCdcUnCommitMetrics());
    }


    @Test
    public void loopSmallTransactionBatches() throws SQLException, InterruptedException {
        t = 50000;
        int gap = 10;
        int loops = 100;

        long i = t;
        long limits = t + gap * loops;

        while (i < limits) {
            Transaction tx = transactionManager.create();
            transactionManager.bind(tx.id());
            try {
                initData(EntityGenerateToolBar.generateFixedEntities(i, 0),  false);
                initData(EntityGenerateToolBar.generateFixedEntities(i, 1), true);
            } catch (Exception ex) {
                tx.rollback();
                throw ex;
            }
            tx.commit();
            transactionManager.finish();
            i += gap;
        }

        Thread.sleep(10000);

        CDCMetrics cdcMetrics = mockRedisCallbackService.queryLastUnCommit();
        Assert.assertNotNull(cdcMetrics);
        Assert.assertNotNull(cdcMetrics.getCdcAckMetrics());

        Assert.assertNotNull(cdcMetrics.getCdcUnCommitMetrics());
    }


    private void initData(IEntity[] datas, boolean replacement) {
        for (IEntity entity : datas) {
            if (!replacement) {
                build(entity);
            } else {
                replace(entity,0);
            }
        }
    }


    private int build(IEntity entity) {
        try {
            Method m1 = masterStorage.getClass()
                    .getDeclaredMethod("toJson", new Class[]{IEntityValue.class});
            m1.setAccessible(true);

            Method m2 = masterStorage.getClass()
                    .getDeclaredMethod("buildSearchAbleSyncMeta", new Class[]{IEntityClass.class});
            m2.setAccessible(true);

            return (int) masterTransactionExecutor.execute(
                new DataSourceNoShardStorageTask(dataSource) {

                        @Override
                        public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                            StorageEntity storageEntity = new StorageEntity();

                            storageEntity.setId(entity.id());
                            storageEntity.setEntity(entity.entityClass().id());
                            if (null != entity.family()) {
                                storageEntity.setPref(entity.family().parent());
                                storageEntity.setCref(entity.family().child());
                            }
                            storageEntity.setTime(entity.time());
                            storageEntity.setCommitid(CommitHelper.getUncommitId());

                            storageEntity.setOp(OperationType.CREATE.getValue());
                            Optional<Transaction> tOp = resource.getTransaction();
                            storageEntity.setTx(tOp.get().id());

                            storageEntity.setDeleted(false);
                            try {
                                storageEntity.setAttribute(
                                        (String) m1.invoke(masterStorage, new Object[]{entity.entityValue()}));

                                storageEntity.setMeta(
                                        (String) m2.invoke(masterStorage, new Object[]{entity.entityClass()}));

                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new SQLException(e.getMessage());
                            }

                            return BuildExecutor.build(
                                    tableName, resource, 0)
                                .execute(storageEntity);
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int replace(IEntity entity, int version) {
        try {
            Method m1 = masterStorage.getClass()
                    .getDeclaredMethod("toJson", new Class[]{IEntityValue.class});
            m1.setAccessible(true);

            Method m2 = masterStorage.getClass()
                    .getDeclaredMethod("buildSearchAbleSyncMeta", new Class[]{IEntityClass.class});
            m2.setAccessible(true);

            return (int) masterTransactionExecutor.execute(
                new DataSourceNoShardStorageTask(dataSource) {

                        @Override
                        public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                            StorageEntity storageEntity = new StorageEntity();

                            storageEntity.setId(entity.id());
                            storageEntity.setEntity(entity.entityClass().id());
                            storageEntity.setPref(entity.family().parent());
                            storageEntity.setCref(entity.family().child());
                            storageEntity.setTime(entity.time());

                            storageEntity.setVersion(version);

                            storageEntity.setOp(OperationType.UPDATE.getValue());
                            Optional<Transaction> tOp = resource.getTransaction();

                            storageEntity.setTx(tOp.get().id());
                            storageEntity.setCommitid(CommitHelper.getUncommitId());

                            storageEntity.setDeleted(false);
                            try {
                                storageEntity.setAttribute(
                                        (String) m1.invoke(masterStorage, new Object[]{entity.entityValue()}));

                                storageEntity.setMeta(
                                        (String) m2.invoke(masterStorage, new Object[]{entity.entityClass()}));

                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new SQLException(e.getMessage());
                            }

                            return ReplaceExecutor.build(
                                    tableName, resource, 0)
                                .execute(storageEntity);
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


}
