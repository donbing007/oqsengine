package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.getEntityClass;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractCDCContainer;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by justin.xu on 05/2021
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS, ContainerType.MYSQL, ContainerType.MANTICORE, ContainerType.CANNAL})
public class ConsumerErrorTest extends AbstractCDCContainer {
    final Logger logger = LoggerFactory.getLogger(ConsumerRunnerTest.class);
    private ConsumerRunner consumerRunner;

    private MockRedisCallbackService mockRedisCallbackService;

    private boolean ifTest = false;

    @Before
    public void before() throws Exception {
        if (ifTest) {
            consumerRunner = initConsumerRunner();
            consumerRunner.start();
        }
    }

    @After
    public void after() throws SQLException {
        if (ifTest) {
            consumerRunner.shutdown();
            clear();
            closeAll();
        }
    }

    private ConsumerRunner initConsumerRunner() throws Exception {
        if (ifTest) {
            ConsumerService consumerService = initAll(true);
            CDCMetricsService cdcMetricsService = new CDCMetricsService();
            mockRedisCallbackService = new MockRedisCallbackService(commitIdStatusService);
            ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

            return new ConsumerRunner(consumerService, cdcMetricsService, singleCDCConnector);
        }
        return null;
    }

    @Test
    public void syncBad() throws Exception {
        if (ifTest) {
            Transaction tx = transactionManager.create(30_000);
            transactionManager.bind(tx.id());

            try {
                IEntity[] entities = EntityGenerateToolBar.generateWithBadEntities(10086, 0);
                initData(tx, entities, false);

                Thread.sleep(1000);

                entities = EntityGenerateToolBar.generateWithBadEntities(10086, 1);
                initData(tx, entities, true);

            } catch (Exception ex) {
                tx.rollback();
                throw ex;
            }

            //将事务正常提交,并从事务管理器中销毁事务.
            tx.commit();
            transactionManager.finish();

            Thread.sleep(50_000);
        }
    }

    private void initData(Transaction tx, IEntity[] datas, boolean replacement) throws SQLException {
        for (IEntity entity : datas) {
            if (replacement) {
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
