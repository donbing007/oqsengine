package com.xforceplus.ultraman.oqsengine.cdc.connect;

import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.getEntityClass;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractCDCTestHelper;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * desc :.
 * name : FailOverTest
 *
 * @author : xujia 2020/11/11
 * @since : 1.8
 */
public class FailOverTest extends AbstractCDCTestHelper {
    private static CDCDaemonService cdcDaemonService;

    private static final int PARTITION = 2000000;

    private static final int MAX = 100;

    private volatile boolean isTetOver = false;

    @BeforeEach
    public void before() throws Exception {
        super.init(true);
        cdcDaemonService = initDaemonService();
    }

    @AfterEach
    public void after() throws Exception {
        super.destroy(true);
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
    private class CDCDamonServiceCall implements Callable {
        @Override
        public Object call() throws Exception {
            System.out.println("start CDCDamonServiceCall thread.");
            while (!isTetOver) {
                cdcDaemonService.init();

                Thread.sleep(20_000);

                cdcDaemonService.destroy();

                Thread.sleep(5_000);
            }
            System.out.println("stop CDCDamonServiceCall thread.");
            return null;
        }
    }

    private class MysqlInitCall implements Callable {

        @Override
        public Object call() throws Exception {
            System.out.println("start MysqlInitCall thread.");
            int i = PARTITION;
            while (!isTetOver) {
                TransactionManager transactionManager = StorageInitialization.getInstance().getTransactionManager();
                Transaction tx = transactionManager.create();
                transactionManager.bind(tx.id());
                try {
                    int j = 0;
                    while (j < MAX) {
                        initData(EntityGenerateToolBar.generateFixedEntities(i + j * 10, 0), false);
                        j++;
                    }
                    j = 0;
                    while (j < MAX) {
                        initData(EntityGenerateToolBar.generateFixedEntities(i + j * 10, 1), true);
                        j++;
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

    private void initData(IEntity[] datas, boolean replacement) throws Exception {
        for (IEntity entity : datas) {
            if (!replacement) {
                MasterDBInitialization.getInstance().getMasterStorage().build(entity, getEntityClass(entity.id()));
            } else {
                MasterDBInitialization.getInstance().getMasterStorage().replace(entity, getEntityClass(entity.id()));
            }
        }
    }
}
