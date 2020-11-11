package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.connect.SingleCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.TestCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceNoShardStorageTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.BuildExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.ReplaceExecutor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;


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

    private TestCallbackService testCallbackService;

    @Before
    public void before() throws Exception {
        initMaster();

        initConsumerRunner();
    }

    private void initConsumerRunner() throws SQLException, InterruptedException {
        CDCMetricsService cdcMetricsService = new CDCMetricsService();
        testCallbackService = new TestCallbackService();
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", testCallbackService);

        SingleCDCConnector singleCDCConnector = new SingleCDCConnector();
        singleCDCConnector.init("localhost",
                environment.getServicePort("canal-server_1", 11111),
                "nly-v1", "root", "xplat");

        consumerRunner = new ConsumerRunner(initConsumerService(), cdcMetricsService, singleCDCConnector);
        consumerRunner.start();
    }

    private long t = 0;

    @Test
    public void syncTest() throws InterruptedException, SQLException {

        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        try {
            initData(EntityGenerateToolBar.generateFixedEntities(t, 0), 1, Long.MAX_VALUE, false);

            Thread.sleep(1000);

            initData(EntityGenerateToolBar.generateFixedEntities(t, 1), 1, 1, true);

        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }

        //将事务正常提交,并从事务管理器中销毁事务.
        tx.commit();
        transactionManager.finish();

        Thread.sleep(10000);

        CDCMetrics cdcMetrics = testCallbackService.queryLastUnCommit();
        Assert.assertNotNull(cdcMetrics);
        Assert.assertNotNull(cdcMetrics.getCdcAckMetrics());

        Assert.assertNotNull(cdcMetrics.getCdcUnCommitMetrics());
    }

    @Test
    public void loopTest() throws InterruptedException, SQLException {
        t = 50;
        int gap = 10;
        long loops = 2;
        int i = 0;
        while (i < loops) {
            Transaction tx = transactionManager.create();
            transactionManager.bind(tx.id());
            try {
                initData(EntityGenerateToolBar.generateFixedEntities(t, 0), 1, Long.MAX_VALUE, false);

                Thread.sleep(1000);

                initData(EntityGenerateToolBar.generateFixedEntities(t, 1), 1, 1, true);

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

        CDCMetrics cdcMetrics = testCallbackService.queryLastUnCommit();
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
        try {
            for (long i = t; i < t + gap * size; i += gap) {
                initData(EntityGenerateToolBar.generateFixedEntities(i, 0), t, Long.MAX_VALUE, false);
            }

            for (long i = t; i < t + gap * size; i += gap) {
                initData(EntityGenerateToolBar.generateFixedEntities(i, 1), t, t, true);
            }
        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }

        tx.commit();
        transactionManager.finish();

        Thread.sleep(30000);

        CDCMetrics cdcMetrics = testCallbackService.queryLastUnCommit();
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
                initData(EntityGenerateToolBar.generateFixedEntities(i, 0), t + i, Long.MAX_VALUE, false);
                initData(EntityGenerateToolBar.generateFixedEntities(i, 1), t + i, t + i, true);
            } catch (Exception ex) {
                tx.rollback();
                throw ex;
            }
            tx.commit();
            transactionManager.finish();
            i += gap;
        }

        Thread.sleep(10000);

        CDCMetrics cdcMetrics = testCallbackService.queryLastUnCommit();
        Assert.assertNotNull(cdcMetrics);
        Assert.assertNotNull(cdcMetrics.getCdcAckMetrics());

        Assert.assertNotNull(cdcMetrics.getCdcUnCommitMetrics());
    }


    private void initData(IEntity[] datas, long tx, long commit, boolean replacement) {
        for (IEntity entity : datas) {
            if (!replacement) {
                build(entity, tx);
            } else {
                replace(entity, tx, commit, 0);
            }
        }
    }


    private int build(IEntity entity, long tx) {
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

                            storageEntity.setTx(tx);
                            storageEntity.setDeleted(false);
                            storageEntity.setCommitid(Long.MAX_VALUE);
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

    private int replace(IEntity entity, long tx, long commit, int version) {
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

                            storageEntity.setTx(tx);
                            storageEntity.setDeleted(false);
                            storageEntity.setCommitid(commit);
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
