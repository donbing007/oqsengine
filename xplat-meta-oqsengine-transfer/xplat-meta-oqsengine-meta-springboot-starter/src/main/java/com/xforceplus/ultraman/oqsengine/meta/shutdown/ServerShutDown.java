package com.xforceplus.ultraman.oqsengine.meta.shutdown;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.ITransferExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

/**
 * desc :
 * name : ServerShutDown
 *
 * @author : xujia
 * date : 2021/2/25
 * @since : 1.8
 */
public class ServerShutDown implements IShutDown {
    private Logger logger = LoggerFactory.getLogger(ServerShutDown.class);

    @Resource
    ITransferExecutor transferExecutor;

    @Resource(name = "grpcServerExecutor")
    private ExecutorService gRpcServerExecutor;

    @Resource(name = "grpcWorkThreadPool")
    private ExecutorService gRpcWorkThreadPool;


    @Override
    public void shutdown() {
        transferExecutor.stop();

        // wait shutdown
        logger.info("Start closing the gRpc server thread...");
        ExecutorHelper.shutdownAndAwaitTermination(gRpcServerExecutor, 3600);
        logger.info("Succeed closing the gRpc server worker thread...ok!");

        // wait shutdown
        logger.info("Start closing the gRpc worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(gRpcWorkThreadPool, 3600);
        logger.info("Succeed closing the gRpc worker thread...ok!");
    }
}
