package com.xforceplus.ultraman.oqsengine.meta.config;

import com.xforceplus.ultraman.oqsengine.meta.IEntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

/**
 * desc :
 * name : ShutDown
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
@Component
public class ShutDown {

    private Logger logger = LoggerFactory.getLogger(ShutDown.class);

    @Resource
    RequestWatchExecutor watchExecutor;

    @Resource(name = "oqsSyncThreadPool")
    private ExecutorService asyncDispatcher;

    @Resource
    private IEntityClassSyncClient entityClassSyncClient;

    @PreDestroy
    public void showdown() {
        watchExecutor.stop();

        entityClassSyncClient.destroy();

        // wait shutdown
        logger.info("Start closing the IO read worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(asyncDispatcher, 3600);
        logger.info("Succeed closing the IO read worker thread...ok!");
    }
}
