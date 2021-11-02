package com.xforceplus.ultraman.oqsengine.boot.shutdown;

import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.RebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 这是为了优雅关闭存在的,目标为第一个创建第一个销毁.
 *
 * @author dongbin
 * @version 0.1 2020/4/1 10:13
 * @since 1.8
 */
@Order(Integer.MIN_VALUE)
@DependsOn({"transactionManager"})
@Component
public class Shutdown {

    final Logger logger = LoggerFactory.getLogger(Shutdown.class);

    @Resource
    private TransactionManager tm;

    @Resource(name = "ioThreadPool")
    private ExecutorService ioThreadPool;

    @Resource(name = "taskThreadPool")
    private ExecutorService taskThreadPool;

    @Resource
    private TaskCoordinator taskCoordinator;

    @Resource
    private CDCDaemonService cdcDaemonService;

    @Resource
    private RebuildIndexExecutor rebuildIndexExecutor;

    @Resource
    private EventBus eventBus;

    @PreDestroy
    public void destroy() throws Exception {

        logger.info("Start closing the process....");

        tm.freeze();
        logger.info("Freeze transactions.");

        // 每次等待时间(秒)
        final int waitTimeSec = 30;
        int size;
        while (true) {
            size = tm.size();
            if (size > 0) {
                logger.info("There are still {} open transactions, waiting {} seconds.", size, waitTimeSec);

                TimeUnit.SECONDS.sleep(waitTimeSec);
            } else {
                break;
            }
        }

        doClose(rebuildIndexExecutor);
        doClose(eventBus);
        doClose(taskCoordinator);
        doClose(cdcDaemonService);

        // wait shutdown
        logger.info("Start closing the io worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(ioThreadPool, 30);
        logger.info("Succeed closing the io worker thread...ok!");

        logger.info("Start closing the task worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(taskThreadPool, 30);
        logger.info("Succeed closing the task worker thread...ok!");

        logger.info("Closing the process......ok!");
    }

    // 关闭.
    private void doClose(Lifecycle lifecycle) throws Exception {
        logger.info("Start shutting down the {}...", lifecycle.getClass().getSimpleName());
        lifecycle.destroy();
        logger.info("Start shutting down the {}...ok!", lifecycle.getClass().getSimpleName());
    }
}
