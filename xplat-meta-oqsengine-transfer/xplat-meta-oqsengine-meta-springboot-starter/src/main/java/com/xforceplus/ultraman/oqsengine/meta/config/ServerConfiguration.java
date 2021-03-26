package com.xforceplus.ultraman.oqsengine.meta.config;

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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper.buildThreadPool;

/**
 * desc :
 * name : ServerConfiguration
 *
 * @author : xujia
 * date : 2021/2/25
 * @since : 1.8
 */
@Configuration
@ConditionalOnProperty(name = "meta.grpc.type", havingValue = "server")
public class ServerConfiguration implements ApplicationContextAware {

    @Autowired
    private ApplicationContext applicationContext;

    private GRpcServer gRpcServer;

    @Bean
    public GRpcServer gRpcServer(
            @Value("${meta.grpc.port}") Integer port
    ) {
        gRpcServer = new GRpcServer(port);

        return gRpcServer;
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

    @Bean(name = "outerBindingService")
    public List<BindableService> bindalbeServices() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(BindGRpcService.class);
        List<BindableService> bindableServices = new ArrayList<>();
        beans.forEach(
                (k, v) -> {
                    if (v instanceof BindableService) {
                        bindableServices.add((BindableService)v);
                    }
                }
        );
        return bindableServices;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
