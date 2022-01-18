package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.getEntityClass;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractCDCTestHelper;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by justin.xu on 05/2021
 */
public class ConsumerErrorTest extends AbstractCDCTestHelper {
    final Logger logger = LoggerFactory.getLogger(ConsumerRunnerTest.class);

    private static final boolean IF_TEST = true;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        if (IF_TEST) {
            super.init(true);
        }
    }

    /**
     * 清理.
     */
    @AfterEach
    public void after() throws Exception {
        if (IF_TEST) {
            super.destroy(true);
        }
    }

    protected ConsumerRunner initConsumerRunner() throws Exception {
        if (IF_TEST) {
            CDCMetricsService cdcMetricsService = new CDCMetricsService();
            mockRedisCallbackService = new MockRedisCallbackService(StorageInitialization.getInstance()
                .getCommitIdStatusService());
            ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

            return new ConsumerRunner(CdcInitialization.getInstance().getConsumerService(),
                cdcMetricsService, CdcInitialization.getInstance().getSingleCDCConnector(), null);
        }
        return null;
    }

    @Test
    public void syncBad() throws Exception {
        if (IF_TEST) {
            TransactionManager transactionManager = StorageInitialization.getInstance().getTransactionManager();
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

    private void initData(Transaction tx, IEntity[] datas, boolean replacement) throws Exception {
        for (IEntity entity : datas) {
            if (replacement) {
                entity.resetVersion(0);
                MasterDBInitialization.getInstance().getMasterStorage().replace(entity, getEntityClass(entity.entityClassRef().getId()));
                tx.getAccumulator().accumulateReplace(entity);
            } else {
                MasterDBInitialization.getInstance().getMasterStorage().build(entity, getEntityClass(entity.entityClassRef().getId()));
                tx.getAccumulator().accumulateBuild(entity);
            }
        }
    }
}
