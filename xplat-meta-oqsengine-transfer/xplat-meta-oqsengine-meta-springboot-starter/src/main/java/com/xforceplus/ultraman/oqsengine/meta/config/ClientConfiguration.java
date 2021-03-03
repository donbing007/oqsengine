package com.xforceplus.ultraman.oqsengine.meta.config;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.connect.MetaSyncGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.ClientShutDown;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.IShutDown;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.ShutDownExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper.buildThreadPool;

/**
 * desc :
 * name : ClientConfiguration
 *
 * @author : xujia
 * date : 2021/2/25
 * @since : 1.8
 */
//@Configuration
public class ClientConfiguration {

    @Bean
    public GRpcClient metaSyncClient(
            @Value("${grpc.server.host}") String host,
            @Value("${grpc.server.port}") int port
    ) {
        MetaSyncGRpcClient metaSyncGRpcClient = new MetaSyncGRpcClient(host, port);
        metaSyncGRpcClient.start();

        return metaSyncGRpcClient;
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
}
