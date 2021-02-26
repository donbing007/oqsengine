package com.xforceplus.ultraman.oqsengine.meta.connect;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncServer;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import io.grpc.ServerBuilder;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.NettyServerBuilder;
import org.lognet.springboot.grpc.GRpcServerBuilderConfigurer;

import javax.annotation.Resource;

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
public class GRpcServer extends GRpcServerBuilderConfigurer {

    @Resource(name = "gRpcServerExecutor")
    private ExecutorService executor;

    @Resource
    private EntityClassSyncServer entityClassSyncServer;

    @Resource
    private GRpcParamsConfig configuration;

    @Override
    public void configure(ServerBuilder<?> serverBuilder) {
        serverBuilder.executor(executor);
        ((NettyServerBuilder) serverBuilder)
                .maxInboundMetadataSize(GrpcUtil.DEFAULT_MAX_HEADER_LIST_SIZE)
                .maxInboundMessageSize(GrpcUtil.DEFAULT_MAX_MESSAGE_SIZE)
                .keepAliveTime(configuration.getDefaultHeartbeatTimeout(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(configuration.getDefaultHeartbeatTimeout(), TimeUnit.MILLISECONDS)
                .permitKeepAliveWithoutCalls(true)
                .permitKeepAliveTime(1, TimeUnit.SECONDS);
    }
}
