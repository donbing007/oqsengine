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

    public GRpcServer(int port) {
        this.port = port;
    }

    @PostConstruct
    @Override
    public void start() {
        entityClassSyncServer.start();
        gRpcServer = serverBuilder().build();
        startDaemonAwaitThread();
    }

    @Override
    public void stop() {
        entityClassSyncServer.stop();
        destroy();
    }

    private void destroy() {
        Optional.ofNullable(gRpcServer).ifPresent(Server::shutdown);
        logger.info("gRPC server stopped.");
    }

    private void startDaemonAwaitThread() {
        Thread awaitThread = ThreadUtils.create(()->{
            try {
                gRpcServer.awaitTermination();
            } catch (InterruptedException e) {
                logger.warn("gRPC server stopped." + e.getMessage());
            }
            return true;
        });
        awaitThread.setDaemon(false);
        awaitThread.start();
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
