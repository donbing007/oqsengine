package com.xforceplus.ultraman.oqsengine.meta.connect;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncServer;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IBasicSyncExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : GRpcServer
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class GRpcServer implements IBasicSyncExecutor {

    private Logger logger = LoggerFactory.getLogger(GRpcServer.class);

    @Resource(name = "grpcServerExecutor")
    private ExecutorService gRpcExecutor;

    @Resource
    private EntityClassSyncServer entityClassSyncServer;

    @Resource
    private GRpcParams configuration;

    @Resource(name = "outerBindingService")
    private List<BindableService> outerServiceList;

    private int port;

    private Server gRpcServer;

    public GRpcServer(int port) {
        this.port = port;
    }


    @Override
    @PostConstruct
    public void start() {
        entityClassSyncServer.start();

        try {
            gRpcServer = serverBuilder().build()
                            .start();
        } catch (IOException e) {
            logger.info("gRpcServer start failed, message : {}", e.getMessage());
            System.exit(-1);
        }

        awaitForTerminationThread();

        logger.info("gRpcServer start ok on port {}.", port);
    }

    @Override
    public void stop() {
        entityClassSyncServer.stop();
        destroy();
    }

    private void destroy() {
        Optional.ofNullable(gRpcServer.isShutdown() ? null : gRpcServer).ifPresent(Server::shutdown);
        logger.info("gRPC server stopped.");
    }

    private void awaitForTerminationThread() {
        Thread serverThread = ThreadUtils.create(()->{
            try {
                gRpcServer.awaitTermination();
            } catch (InterruptedException e) {
                logger.warn("gRPC server stopped failed, {}", e.getMessage());
            }
            return true;
        });
        serverThread.setDaemon(false);

        serverThread.start();
    }

    private ServerBuilder serverBuilder() {
        logger.info("server build start...");
        ServerBuilder serverBuilder = NettyServerBuilder.forPort(port)
                        .executor(gRpcExecutor)
                        .addService(entityClassSyncServer)
                        .maxInboundMetadataSize(GrpcUtil.DEFAULT_MAX_HEADER_LIST_SIZE)
                        .maxInboundMessageSize(GrpcUtil.DEFAULT_MAX_MESSAGE_SIZE)
                        .keepAliveTime(configuration.getDefaultHeartbeatTimeout(), TimeUnit.MILLISECONDS)
                        .keepAliveTimeout(configuration.getDefaultHeartbeatTimeout(), TimeUnit.MILLISECONDS)
                        .permitKeepAliveWithoutCalls(true)
                        .permitKeepAliveTime(1, TimeUnit.SECONDS);

        if (null != outerServiceList) {
            outerServiceList.forEach(
                    o -> {
                        serverBuilder.addService(o);
                        logger.info("server build, add outerService [{}]", o.getClass().getCanonicalName());
                    }
            );

        }

        logger.info("server build ok...");

        return serverBuilder;
    }
}
