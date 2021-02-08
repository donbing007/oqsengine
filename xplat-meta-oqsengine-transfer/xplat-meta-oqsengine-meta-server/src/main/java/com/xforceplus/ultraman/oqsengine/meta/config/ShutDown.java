package com.xforceplus.ultraman.oqsengine.meta.config;

import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
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
    ResponseWatchExecutor watchExecutor;

    @Resource(name = "gRpcServerExecutor")
    private ExecutorService gRpcServerExecutor;

    @Resource(name = "gRpcTaskExecutor")
    private ExecutorService gRpcTaskExecutor;


    @Resource
    private SyncResponseHandler responseHandler;

    @PreDestroy
    public void showdown() {
        watchExecutor.stop();

        responseHandler.stop();

        // wait shutdown
        logger.info("Start closing the gRpc server worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(gRpcServerExecutor, 3600);
        logger.info("Succeed closing the gRpc server worker thread...ok!");

        // wait shutdown
        logger.info("Start closing the gRpc task worker thread...");
        ExecutorHelper.shutdownAndAwaitTermination(gRpcTaskExecutor, 3600);
        logger.info("Succeed closing the gRpc task worker thread...ok!");
    }
}
