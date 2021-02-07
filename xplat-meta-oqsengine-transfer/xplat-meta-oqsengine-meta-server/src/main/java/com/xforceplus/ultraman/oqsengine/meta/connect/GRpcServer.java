package com.xforceplus.ultraman.oqsengine.meta.connect;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncServer;

import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import org.lognet.springboot.grpc.GRpcServerBuilderConfigurer;

import javax.annotation.Resource;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : GRpcServer
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public class GRpcServer extends GRpcServerBuilderConfigurer {

    @Resource(name = "gRpcServerExecutor")
    private Executor executor;

    @Resource
    private EntityClassSyncServer entityClassSyncServer;

    @Resource
    private GRpcServerConfiguration configuration;

    @Override
    public void configure(ServerBuilder<?> serverBuilder) {
        serverBuilder.executor(executor);
        ((NettyServerBuilder) serverBuilder)
                .maxInboundMetadataSize(configuration.getMaxInboundMetadataBytes())
                .maxInboundMessageSize(configuration.getMaxInboundMessageBytes())
                .keepAliveTime(configuration.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS)
                .keepAliveTimeout(configuration.getHeartbeatTimeoutSeconds(), TimeUnit.SECONDS)
                .permitKeepAliveWithoutCalls(true)
                .permitKeepAliveTime(1, TimeUnit.SECONDS);
    }
}
