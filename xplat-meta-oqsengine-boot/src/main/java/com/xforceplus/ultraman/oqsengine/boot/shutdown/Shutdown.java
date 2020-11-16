package com.xforceplus.ultraman.oqsengine.boot.shutdown;

import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import io.lettuce.core.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 这是为了优雅关闭存在的,目标为第一个创建第一个销毁.
 *
 * @author dongbin
 * @version 0.1 2020/4/1 10:13
 * @since 1.8
 */
@Order(Integer.MIN_VALUE)
@DependsOn("transactionManager")
@Component
public class Shutdown {

    final Logger logger = LoggerFactory.getLogger(Shutdown.class);

    @Resource
    private TransactionManager tm;

    @Resource(name = "callThreadPool")
    private ExecutorService callThreadPool;

    @Resource(name = "cdcConsumerPool")
    private ExecutorService cdcConsumerPool;

    @Resource
    private CDCDaemonService cdcDaemonService;

    @Resource
    private RedisClient redisClient;

    @Resource
    private DataSourcePackage dataSourcePackage;


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

        // wait shutdown
        logger.info("Start closing the IO worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(callThreadPool, 3600);
        logger.info("Succeed closing the IO worker thread...ok!");


        logger.info("Start closing the consumer worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(cdcConsumerPool, 3600);
        logger.info("Succeed closing the consumer worker thread...ok!");

        logger.info("Start closing the cdc consumer service...");
        cdcDaemonService.stopDaemon();
        logger.info("Succeed closing thd cdc consumer service...ok!");

        logger.info("Start closing the redis client...");
        redisClient.shutdown(Duration.ofMillis(3000), Duration.ofSeconds(3600));
        logger.info("Succeed closing the redis client...ok!");

        logger.info("Start closing the datasource...");
        dataSourcePackage.close();
        logger.info("Succeed closing the datasource...ok!");

        logger.info("Closing the process......ok!");
    }
}
