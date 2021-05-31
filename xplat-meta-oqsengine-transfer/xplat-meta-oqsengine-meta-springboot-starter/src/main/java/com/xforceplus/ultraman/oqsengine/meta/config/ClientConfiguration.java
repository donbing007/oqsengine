package com.xforceplus.ultraman.oqsengine.meta.config;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.connect.MetaSyncGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.ClientShutDown;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.IShutDown;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 客户端配置.
 *
 * @author xujia
 * @since 1.8
 */
@Configuration
@ConditionalOnProperty(name = "meta.grpc.type", havingValue = "client")
public class ClientConfiguration {

    @Bean
    public GRpcClient grpcClient(
        @Value("${meta.grpc.host}") String host,
        @Value("${meta.grpc.port}") int port
    ) {
        return new MetaSyncGRpcClient(host, port);
    }

    @Bean
    public IRequestHandler requestHandler() {
        return new SyncRequestHandler();
    }

    @Bean
    public IRequestWatchExecutor requestWatchExecutor() {
        return new RequestWatchExecutor();
    }

    @Bean
    public EntityClassSyncClient entityClassSyncClient() {
        return new EntityClassSyncClient();
    }

    @Bean(name = "grpcShutdown")
    public IShutDown clientShutDown() {
        return new ClientShutDown();
    }

}
