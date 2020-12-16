package com.xforceplus.ultraman.oqsengine.cdc.connect;

import com.xforceplus.ultraman.oqsengine.cdc.CDCAbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
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
public class FailOverTest extends CDCAbstractContainer {
    private MockRedisCallbackService mockRedisCallbackService;

    private CDCDaemonService cdcDaemonService;

    private static final int partition = 2000000;

    private static final int max = 100;

    private volatile boolean isTetOver = false;

    @Before
    public void before() throws Exception {
        initDaemonService();
    }

    @After
    public void after() throws SQLException {
        clear();
        closeAll();
    }

    private void initDaemonService() throws Exception {
        CDCMetricsService cdcMetricsService = new CDCMetricsService();
        mockRedisCallbackService = new MockRedisCallbackService();
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

        cdcDaemonService = new CDCDaemonService();
        ReflectionTestUtils.setField(cdcDaemonService, "nodeIdGenerator", new StaticNodeIdGenerator(ZERO));
        ReflectionTestUtils.setField(cdcDaemonService, "consumerService", initAll());
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
        Thread.sleep(100_000);
        System.out.println("stop");
        isTetOver = true;
        //  继续等待10秒，结束
        Thread.sleep(10_000);
    }

    //  模拟5秒宕机, 5秒后恢复
    public class CDCDamonServiceCall implements Callable {
        @Override
        public Object call() throws Exception {
            System.out.println("start CDCDamonServiceCall thread.");
            while (!isTetOver) {
                cdcDaemonService.startDaemon();

                Thread.sleep(20_000);

                cdcDaemonService.stopDaemon();

                Thread.sleep(5_000);
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
                } catch (Exception ex) {
                    tx.rollback();
                    throw ex;
                }
                tx.commit();
                transactionManager.finish();

                i += 1200 + 10;

                Thread.sleep(5_000);
            }
            System.out.println("stop MysqlInitCall thread.");
            return null;
        }
    }

    private void initData(IEntity[] datas, boolean replacement) throws SQLException {
        for (IEntity entity : datas) {
            if (!replacement) {
                masterStorage.build(entity);
            } else {
                masterStorage.replace(entity);
            }
        }
    }
}
