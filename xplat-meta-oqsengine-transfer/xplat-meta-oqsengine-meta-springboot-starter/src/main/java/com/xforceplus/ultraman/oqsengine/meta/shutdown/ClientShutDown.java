package com.xforceplus.ultraman.oqsengine.meta.shutdown;

import com.xforceplus.ultraman.oqsengine.meta.IEntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.config.ShutDown;
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
public class ClientShutDown implements IShutDown {

    private Logger logger = LoggerFactory.getLogger(ShutDown.class);

    @Resource(name = "grpcWorkThreadPool")
    private ExecutorService metaSyncThreadPool;

    @Resource
    private IEntityClassSyncClient entityClassSyncClient;

    @Override
    public void shutdown() {

        entityClassSyncClient.destroy();

        // wait shutdown
        logger.info("Start closing the gRpc worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(metaSyncThreadPool, 3600);
        logger.info("Succeed closing the gRpc worker thread...ok!");
    }
}
