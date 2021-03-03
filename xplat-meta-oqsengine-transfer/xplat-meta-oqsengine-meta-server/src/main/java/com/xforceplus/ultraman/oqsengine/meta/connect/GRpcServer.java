package com.xforceplus.ultraman.oqsengine.meta.connect;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncServer;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IBasicSyncExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.io.IOException;
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
    private GRpcParamsConfig configuration;

    private int port;

    private Server gRpcServer;

    public volatile boolean isStart = false;

    public GRpcServer(int port) {
        this.port = port;
    }

    @PostConstruct
    @Override
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

        isStart = true;
        logger.info("gRpcServer start ok on port {}.", port);
    }

    @Override
    public void stop() {
        entityClassSyncServer.stop();
        isStart = false;
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
        return NettyServerBuilder.forPort(port)
                        .executor(gRpcExecutor)
                        .addService(entityClassSyncServer)
                        .maxInboundMetadataSize(GrpcUtil.DEFAULT_MAX_HEADER_LIST_SIZE)
                        .maxInboundMessageSize(GrpcUtil.DEFAULT_MAX_MESSAGE_SIZE)
                        .keepAliveTime(configuration.getDefaultHeartbeatTimeout(), TimeUnit.MILLISECONDS)
                        .keepAliveTimeout(configuration.getDefaultHeartbeatTimeout(), TimeUnit.MILLISECONDS)
                        .permitKeepAliveWithoutCalls(true)
                        .permitKeepAliveTime(1, TimeUnit.SECONDS);
    }
}
