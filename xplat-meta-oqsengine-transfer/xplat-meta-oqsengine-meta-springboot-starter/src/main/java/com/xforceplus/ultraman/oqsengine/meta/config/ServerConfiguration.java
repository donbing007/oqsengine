package com.xforceplus.ultraman.oqsengine.meta.config;


import static com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper.buildThreadPool;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncServer;
import com.xforceplus.ultraman.oqsengine.meta.annotation.BindGRpcService;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServer;
import com.xforceplus.ultraman.oqsengine.meta.executor.IResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IResponseHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import com.xforceplus.ultraman.oqsengine.meta.listener.EntityClassListener;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.IShutDown;
import com.xforceplus.ultraman.oqsengine.meta.shutdown.ServerShutDown;
import io.grpc.BindableService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 服务端配置.
 *
 * @author xujia
 * @since 1.8
 */
@Configuration
@ConditionalOnProperty(name = "meta.grpc.type", havingValue = "server")
public class ServerConfiguration implements ApplicationContextAware {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public GRpcServer grpcServer(
        @Value("${meta.grpc.port}") Integer port
    ) {
        return new GRpcServer(port);
    }

    @Bean
    public IResponseWatchExecutor watchExecutor() {
        return new ResponseWatchExecutor();
    }

    @Bean
    public IDelayTaskExecutor<RetryExecutor.DelayTask> retryExecutor() {
        return new RetryExecutor();
    }

    @Bean
    public IResponseHandler responseHandler() {
        return new SyncResponseHandler();
    }

    @Bean
    public EntityClassSyncServer entityClassSyncServer() {
        return new EntityClassSyncServer();
    }

    /**
     * 初始化线程池.
     */
    @Bean("grpcServerExecutor")
    public ExecutorService callGRpcThreadPool(
        @Value("${threadPool.call.grpc.server.worker:0}") int worker,
        @Value("${threadPool.call.grpc.server.queue:500}") int queue) {
        int useWorker = worker;
        int useQueue = queue;
        if (useWorker == 0) {
            useWorker = Runtime.getRuntime().availableProcessors() + 1;
        }

        if (useQueue < 500) {
            useQueue = 500;
        }

        return buildThreadPool(useWorker, useQueue, "grpc-server-call", false);
    }

    @Bean
    public EntityClassListener entityClassListener() {
        return new EntityClassListener();
    }

    @Bean(name = "grpcShutdown")
    public IShutDown serverShutDown() {
        return new ServerShutDown();
    }

    /**
     * 初始化外部服务.
     */
    @Bean(name = "outerBindingService")
    public List<BindableService> bindAbleServices() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(BindGRpcService.class);
        List<BindableService> bindAbleServices = new ArrayList<>();
        beans.forEach(
            (k, v) -> {
                if (v instanceof BindableService) {
                    bindAbleServices.add((BindableService) v);
                }
            }
        );
        return bindAbleServices;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
