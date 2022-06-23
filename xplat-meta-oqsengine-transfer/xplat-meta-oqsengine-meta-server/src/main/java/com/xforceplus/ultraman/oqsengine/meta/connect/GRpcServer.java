package com.xforceplus.ultraman.oqsengine.meta.connect;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncServer;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IBasicSyncExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import io.grpc.BindableService;
import io.grpc.Grpc;
import io.grpc.HttpConnectProxiedSocketAddress;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.NettyServerBuilder;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * grpc client implement.
 *
 * @author xujia
 * @since 1.8
 */
public class GRpcServer implements IBasicSyncExecutor {

    private final Logger logger = LoggerFactory.getLogger(GRpcServer.class);

    @Resource(name = "grpcServerExecutor")
    private ExecutorService grpcExecutor;

    @Resource
    private EntityClassSyncServer entityClassSyncServer;

    @Resource
    private GRpcParams configuration;

    @Resource(name = "outerBindingService")
    private List<BindableService> outerServiceList;

    private final int port;

    private Server grpcServer;

    public GRpcServer(int port) {
        this.port = port;
    }

    private boolean interceptOn = true;

    public void setInterceptOn(boolean interceptOn) {
        this.interceptOn = interceptOn;
    }

    @Override
    @PostConstruct
    public void start() {
        entityClassSyncServer.start();

        try {
            ServerBuilder serverBuilder = serverBuilder();
            if (interceptOn) {
                serverBuilder.intercept(new ServerInterceptor() {
                    Metadata.Key<String> metaClientId = Metadata.Key.of("clientId", Metadata.ASCII_STRING_MARSHALLER);

                    @Override
                    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                                 Metadata headers,
                                                                                 ServerCallHandler<ReqT, RespT> next) {


                        SocketAddress socketAddress = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);

                        String clientIdString = headers.get(metaClientId);
                        if (null == clientIdString || clientIdString.isEmpty()) {
                            clientIdString = "unknown clientId";
                        }

                        logger.info("ip : {}, clientId : {}, methodName : {}"
                            , null != socketAddress ? socketAddress.toString() : "unknown"
                            , clientIdString
                            , call.getMethodDescriptor().getFullMethodName()
                            );

                        return next.startCall(call, headers);
                    }
                });
            }

            grpcServer = serverBuilder.build().start();
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
        Optional.ofNullable(grpcServer.isShutdown() ? null : grpcServer).ifPresent(Server::shutdown);
        logger.info("gRPC server stopped.");
    }

    private void awaitForTerminationThread() {
        Thread serverThread = ThreadUtils.create(() -> {
            try {
                grpcServer.awaitTermination();
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
            .executor(grpcExecutor)
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
