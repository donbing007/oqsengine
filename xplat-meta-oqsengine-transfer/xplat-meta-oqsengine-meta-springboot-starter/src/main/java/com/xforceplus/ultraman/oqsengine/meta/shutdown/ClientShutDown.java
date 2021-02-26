package com.xforceplus.ultraman.oqsengine.meta.shutdown;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.ITransferExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Logger logger = LoggerFactory.getLogger(ClientShutDown.class);

    @Resource(name = "grpcWorkThreadPool")
    private ExecutorService metaSyncThreadPool;

    @Resource
    private ITransferExecutor entityClassSyncClient;

    @Override
    public void shutdown() {

        entityClassSyncClient.stop();

        // wait shutdown
        logger.info("Start closing the gRpc worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(metaSyncThreadPool, 3600);
        logger.info("Succeed closing the gRpc worker thread...ok!");
    }
}
