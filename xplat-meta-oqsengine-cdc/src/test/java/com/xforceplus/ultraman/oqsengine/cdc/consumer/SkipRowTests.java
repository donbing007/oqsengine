package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.cdc.CDCAbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.common.cdc.SkipRow;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.status.impl.CDCStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
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
import java.util.HashMap;
import java.util.Map;

import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.getEntityClass;

/**
 * Created by justin.xu on 04/2021
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS, ContainerType.MYSQL, ContainerType.MANTICORE, ContainerType.CANNAL})
public class SkipRowTests extends CDCAbstractContainer {
    final Logger logger = LoggerFactory.getLogger(SkipRowTests.class);

    private ConsumerRunner consumerRunner;
    private MockRedisCallbackService mockRedisCallbackService;
    private CDCStatusServiceImpl cdcStatusService;

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
        ConsumerService consumerService = initAll(false);
        CDCMetricsService cdcMetricsService = new CDCMetricsService();

        cdcStatusService = new CDCStatusServiceImpl();
        ReflectionTestUtils.setField(cdcStatusService, "redisClient", redisClient);
        ReflectionTestUtils.setField(cdcStatusService, "objectMapper", new ObjectMapper());
        cdcStatusService.init();

        mockRedisCallbackService = new MockRedisCallbackService(commitIdStatusService, cdcStatusService);
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

        return new ConsumerRunner(consumerService, cdcMetricsService, singleCDCConnector);
    }

    private Map<String, String> expectedSkips = new HashMap<>();

    @Test
    public void testSkip() throws Exception {
        init();

        checkAfterLoops("testSkip");
    }

    private int expectedCount = 0;

    private void init() throws Exception {
        Transaction tx = transactionManager.create(30_000);
        transactionManager.bind(tx.id());

        try {
            IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(1, 0);
            initData(tx, entities, false, false);

            Thread.sleep(1000);

            entities = EntityGenerateToolBar.generateFixedEntities(1, 1);

            initData(tx, entities, true, false);

            expectedSkips.putIfAbsent(SkipRow.toSkipRow(1, entities[2].id(), entities[2].version() + 1, OperationType.UPDATE.getValue()), "1");
            cdcStatusService.addSkipRow(1, entities[2].id(), entities[2].version() + 1, OperationType.UPDATE.getValue(), true);

            expectedSkips.putIfAbsent(SkipRow.toSkipRow(1, entities[5].id(), entities[5].version() + 1, OperationType.UPDATE.getValue()), "1");
            cdcStatusService.addSkipRow(1, entities[5].id(), entities[5].version() + 1, OperationType.UPDATE.getValue(), true);

            expectedCount = entities.length - 2;

        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }

        check();

        //将事务正常提交,并从事务管理器中销毁事务.
        tx.commit();
        transactionManager.finish();
    }

    private void check() {
        Map<String, String> queries = cdcStatusService.querySkipRows();
        for (Map.Entry<String, String> entry : queries.entrySet()) {
            String v = expectedSkips.remove(entry.getKey());
            Assert.assertEquals(v, entry.getValue());
        }
    }

    private void checkAfterLoops(String func) throws InterruptedException {
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

        Map<String, String> queries = cdcStatusService.querySkipRows();
        Assert.assertTrue(null == queries || queries.isEmpty());
    }

    private void initData(Transaction tx, IEntity[] datas, boolean replacement, boolean delete) throws SQLException {
        for (IEntity entity : datas) {
            if (delete) {
                masterStorage.delete(entity, getEntityClass(entity.entityClassRef().getId()));
                tx.getAccumulator().accumulateDelete(entity);
            } else if (replacement) {
                entity.resetVersion(0);
                masterStorage.replace(entity, getEntityClass(entity.entityClassRef().getId()));
                tx.getAccumulator().accumulateReplace(entity, entity);
            } else {
                masterStorage.build(entity, getEntityClass(entity.entityClassRef().getId()));
                tx.getAccumulator().accumulateBuild(entity);
            }
        }
    }
}
