package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.connect.SingleCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceNoShardResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.BuildExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.ReplaceExecutor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.*;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;


/**
 * desc :
 * name : FailOverTest
 *
 * @author : xujia
 * date : 2020/11/11
 * @since : 1.8
 */
public class FailOverTest extends AbstractContainer {
    private MockRedisCallbackService mockRedisCallbackService;

    private CDCDaemonService cdcDaemonService;

    private static final int partition = 10000000;

    private static final int max = 100;

    private boolean isTetOver = false;

    @Before
    public void before() throws Exception {

        initMaster();

        initDaemonService();
    }


    private void initDaemonService() throws SQLException, InterruptedException {
        CDCMetricsService cdcMetricsService = new CDCMetricsService();
        mockRedisCallbackService = new MockRedisCallbackService();
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

        SingleCDCConnector singleCDCConnector = new SingleCDCConnector();
        singleCDCConnector.init(System.getProperty("CANAL_HOST"), Integer.parseInt(System.getProperty("CANAL_PORT")),
                "nly-v1", "root", "xplat");

        cdcDaemonService = new CDCDaemonService();
        ReflectionTestUtils.setField(cdcDaemonService, "nodeIdGenerator", new StaticNodeIdGenerator(ZERO));
        ReflectionTestUtils.setField(cdcDaemonService, "consumerService", initConsumerService());
        ReflectionTestUtils.setField(cdcDaemonService, "cdcMetricsService", cdcMetricsService);
        ReflectionTestUtils.setField(cdcDaemonService, "cdcConnector", singleCDCConnector);
    }

    @Test
    public void testFailOver() throws InterruptedException {
        ExecutorService executorService = new ThreadPoolExecutor(2, 2, 0,
        TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        executorService.submit(new MysqlInitCall());
        executorService.submit(new CDCDamonServiceCall());


        //  睡眠120秒，结束
        Thread.sleep(120 * 1000);
        isTetOver = false;
        //  继续等待10秒，结束
        Thread.sleep(10 * 1000);
    }

    //  模拟5秒宕机, 5秒后恢复
    public class CDCDamonServiceCall implements Callable {
        @Override
        public Object call() throws Exception {
            System.out.println("start CDCDamonServiceCall thread.");
            while (!isTetOver) {
                cdcDaemonService.startDaemon();

                Thread.sleep(20 * 1000);

                cdcDaemonService.stopDaemon();
            }
            System.out.println("stop CDCDamonServiceCall thread.");
            return null;
        }
    }

    public class MysqlInitCall implements Callable {

        @Override
        public Object call() throws Exception {
            System.out.println("start MysqlInitCall thread.");
            int i = partition;
            while (!isTetOver) {
                Transaction tx = transactionManager.create();
                transactionManager.bind(tx.id());
                try {
                    int j = 0;
                    while (j < max) {
                        initData(EntityGenerateToolBar.generateFixedEntities(i + j * 10, 0), false);
                        j ++;
                    }
                    j = 0;
                    while (j < max) {
                        initData(EntityGenerateToolBar.generateFixedEntities(i + j * 10, 1), true);
                        j ++;
                    }
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    tx.rollback();
                    throw ex;
                }
                tx.commit();
                transactionManager.finish();

                i += 1200 + 10;
            }


            System.out.println("stop MysqlInitCall thread.");
            return null;
        }
    }



    private void initData(IEntity[] datas, boolean replacement) {
        for (IEntity entity : datas) {
            if (!replacement) {
                build(entity);
            } else {
                replace(entity, 0);
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
                new DataSourceNoShardResourceTask(dataSource) {

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
                new DataSourceNoShardResourceTask(dataSource) {

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
