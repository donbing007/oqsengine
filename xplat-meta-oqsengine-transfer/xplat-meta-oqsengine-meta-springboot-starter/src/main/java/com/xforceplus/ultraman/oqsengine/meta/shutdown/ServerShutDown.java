package com.xforceplus.ultraman.oqsengine.meta.shutdown;

import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServer;
import java.util.concurrent.ExecutorService;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 服务端关闭.
 *
 * @author xujia
 * @since 1.8
 */
public class ServerShutDown implements IShutDown {
    private final Logger logger = LoggerFactory.getLogger(ServerShutDown.class);

    @Resource
    GRpcServer grpcServer;

    @Resource(name = "grpcServerExecutor")
    private ExecutorService grpcServerExecutor;

    @Resource(name = "grpcTaskExecutor")
    private ExecutorService grpcTaskExecutor;


    @Override
    public void shutdown() {
        logger.info("meta sync server tear down...");
        grpcServer.stop();

        // wait shutdown
        logger.info("Start closing the gRpc server thread...");
        ExecutorHelper.shutdownAndAwaitTermination(grpcServerExecutor, 3600);
        logger.info("Succeed closing the gRpc server worker thread...ok!");

        // wait shutdown
        logger.info("Start closing the gRpc worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(grpcTaskExecutor, 3600);
        logger.info("Succeed closing the gRpc worker thread...ok!");
    }
}
