package com.xforceplus.ultraman.oqsengine.boot.shutdown;

import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.RebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.event.DefaultEventBus;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import io.lettuce.core.RedisClient;
import java.time.Duration;
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

    @Resource(name = "callReadThreadPool")
    private ExecutorService callReadThreadPool;

    @Resource(name = "callWriteThreadPool")
    private ExecutorService callWriteThreadPool;

    @Resource(name = "callRebuildThreadPool")
    private ExecutorService callRebuildThreadPool;

    @Resource(name = "eventWorker")
    private ExecutorService eventWorker;

    @Resource(name = "waitVersionExecutor")
    private ExecutorService waitVersionExecutor;

    @Resource
    private CDCDaemonService cdcDaemonService;

    @Resource
    private RedisClient redisClient;

    @Resource
    private RedisClient redisClientChangeLog;

    @Resource
    private RedisClient redisClientCacheEvent;

    @Resource
    private DataSourcePackage dataSourcePackage;

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

        rebuildIndexExecutor.destroy();

        if (DefaultEventBus.class.equals(eventBus.getClass())) {
            ((DefaultEventBus) eventBus).destroy();
        }

        // wait shutdown
        logger.info("Start closing the eventWorker worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(eventWorker, 3600);
        logger.info("Succeed closing the eventWorker worker thread...ok!");

        logger.info("Start closing the waitVersionExecutor worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(waitVersionExecutor, 3600);
        logger.info("Succeed closing the waitVersionExecutor worker thread...ok!");

        logger.info("Start closing the IO read worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(callReadThreadPool, 3600);
        logger.info("Succeed closing the IO read worker thread...ok!");

        logger.info("Start closing the IO write worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(callWriteThreadPool, 3600);
        logger.info("Succeed closing the IO write worker thread...ok!");

        logger.info("Start closing the callRebuild worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(callRebuildThreadPool, 3600);
        logger.info("Succeed closing the callRebuild worker thread...ok!");

        logger.info("Start closing the cdc consumer service...");
        cdcDaemonService.stopDaemon();
        logger.info("Succeed closing thd cdc consumer service...ok!");

        logger.info("Start closing the redis client...");
        redisClient.shutdown(Duration.ofMillis(3000), Duration.ofSeconds(3600));
        logger.info("Succeed closing the redis client...ok!");

        logger.info("Start closing the redis client for change-log...");
        redisClientChangeLog.shutdown(Duration.ofMillis(3000), Duration.ofSeconds(3600));
        logger.info("Succeed closing the redis client for change-log...ok!");

        logger.info("Start closing the redis client for cache-event...");
        redisClientCacheEvent.shutdown(Duration.ofMillis(3000), Duration.ofSeconds(3600));
        logger.info("Succeed closing the redis client for cache-event...ok!");

        logger.info("Start closing the datasource...");
        dataSourcePackage.close();
        logger.info("Succeed closing the datasource...ok!");

        logger.info("Closing the process......ok!");
    }
}
