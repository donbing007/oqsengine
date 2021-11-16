package com.xforceplus.ultraman.oqsengine.meta.shutdown;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import java.util.concurrent.ExecutorService;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 客户端关闭.
 *
 * @author xujia
 * @since 1.8
 */
public class ClientShutDown implements IShutDown {

    private final Logger logger = LoggerFactory.getLogger(ClientShutDown.class);

    @Resource(name = "grpcTaskExecutor")
    private ExecutorService grpcTaskExecutor;

    @Resource
    private EntityClassSyncClient entityClassSyncClient;

    @Override
    public void shutdown() {
        logger.info("meta sync client tear down...");
        entityClassSyncClient.stop();

        // wait shutdown
        logger.info("Start closing the gRpc worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(grpcTaskExecutor, 3600);
        logger.info("Succeed closing the gRpc worker thread...ok!");
    }
}
