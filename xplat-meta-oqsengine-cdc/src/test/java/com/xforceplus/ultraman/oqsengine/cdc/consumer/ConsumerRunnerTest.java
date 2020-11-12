package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.connect.SingleCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

    private long t = 0;
    private int expectedCount = 0;

    @Test
    public void syncTest() throws InterruptedException, SQLException {
        t = 1;
        expectedCount = 0;
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        int expectedCount = 0;
        try {
            IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(t, 0);
            initData(entities, false);

            Thread.sleep(1000);

            entities = EntityGenerateToolBar.generateFixedEntities(t, 1);
            initData(entities, true);
            expectedCount = entities.length;

        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }

        //将事务正常提交,并从事务管理器中销毁事务.
        tx.commit();
        transactionManager.finish();

        Thread.sleep(10 * 1000);

        CDCMetrics cdcMetrics = mockRedisCallbackService.queryLastUnCommit();
        Assert.assertNotNull(cdcMetrics);
        Assert.assertNotNull(cdcMetrics.getCdcAckMetrics());

        Assert.assertNotNull(cdcMetrics.getCdcUnCommitMetrics());
        Assert.assertEquals(expectedCount, mockRedisCallbackService.getExecuted().get());
    }

    @Test
    public void loopTest() throws InterruptedException, SQLException {
        t = 50;
        int gap = 10;
        long loops = 2;
        int i = 0;
        expectedCount = 0;

        mockRedisCallbackService.reset();
        while (i < loops) {
            Transaction tx = transactionManager.create();
            transactionManager.bind(tx.id());
            try {
                IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(t, 0);
                initData(entities, false);

                Thread.sleep(1000);

                entities = EntityGenerateToolBar.generateFixedEntities(t, 1);
                initData(entities, true);
                expectedCount += entities.length;
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
        Assert.assertEquals(expectedCount, mockRedisCallbackService.getExecuted().get());
    }

    @Test
    public void loopTransactionOverBatches() throws SQLException, InterruptedException {
        t = 15000;
        int gap = 10;
        int size = 250;

        mockRedisCallbackService.reset();
        expectedCount = 0;


        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());

        try {
            IEntity[] entities;
            for (long i = t; i < t + gap * size; i += gap) {
                entities = EntityGenerateToolBar.generateFixedEntities(i, 0);
                initData(entities, false);
                expectedCount += entities.length;
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
        Assert.assertEquals(expectedCount, mockRedisCallbackService.getExecuted().get());
    }


    @Test
    public void loopSmallTransactionBatches() throws SQLException, InterruptedException {
        t = 50000;
        int gap = 10;
        int loops = 100;

        long i = t;
        long limits = t + gap * loops;

        mockRedisCallbackService.reset();
        expectedCount = 0;

        while (i < limits) {
            Transaction tx = transactionManager.create();
            transactionManager.bind(tx.id());
            try {
                IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(i, 0);
                initData(entities,  false);
                initData(EntityGenerateToolBar.generateFixedEntities(i, 1), true);

                expectedCount += entities.length;
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
        Assert.assertEquals(expectedCount, mockRedisCallbackService.getExecuted().get());
    }


    private void initData(IEntity[] datas, boolean replacement) throws SQLException {
        for (IEntity entity : datas) {
            if (!replacement) {
                build(entity);
            } else {
                replace(entity,0);
            }
        }
    }


    private int build(IEntity entity) throws SQLException {
        return masterStorage.build(entity);
    }

    private int replace(IEntity entity, int version) throws SQLException {
        entity.resetVersion(version);
        return masterStorage.replace(entity);
    }


}
