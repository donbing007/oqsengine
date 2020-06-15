package com.xforceplus.ultraman.oqsengine.sdk.config.init;

import akka.stream.ActorMaterializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Sink;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.j2objc.annotations.AutoreleasePool;
import com.xforceplus.ultraman.config.ConfigurationEngine;
import com.xforceplus.ultraman.config.json.JsonConfigNode;
import com.xforceplus.ultraman.metadata.grpc.CheckServiceClient;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.event.MetadataModuleGotEvent;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.xplat.galaxy.grpc.client.LongConnect;
import io.reactivex.Observable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * module grpc init service
 */
@Order
public class ModuleInitService implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(ModuleInitService.class);

    private final CheckServiceClient checkServiceClient;

    private final ActorMaterializer mat;

    private final AuthSearcherConfig config;

    private final ApplicationEventPublisher publisher;

    public ModuleInitService(CheckServiceClient checkServiceClient
            , ActorMaterializer mat, AuthSearcherConfig config
            , ApplicationEventPublisher publisher) {
        this.checkServiceClient = checkServiceClient;
        this.mat = mat;
        this.config = config;
        this.publisher = publisher;
    }

    @Autowired
    MetadataRepository store;

    @Value("${xplat.oqsengine.sdk.init-size:5}")
    private Integer size;

    @Value("${xplat.oqsengine.sdk.init-time:10}")
    private Integer time;

    @Autowired
    private ConfigurationEngine<ModuleUpResult, JsonConfigNode> moduleConfigEngine;

    @Override
    public void afterPropertiesSet() throws Exception {

        com.xforceplus.ultraman.metadata.grpc.Base.Authorization request = com.xforceplus
                .ultraman.metadata.grpc.Base.Authorization.newBuilder()
                .setAppId(config.getAppId())
                .setEnv(config.getEnv())
                .setTenantId(config.getTenant())
                .build();

        Publisher<List<ModuleUpResult>> moduleService = LongConnect.safeSource(2, 20
                , () -> checkServiceClient.checkStreaming(request))
                .log("ModuleService")
                .groupedWithin(size, Duration.ofSeconds(time))
                .map(x -> {
                    logger.info("Got module size {}", x.size());
                    return x;
                })
                .runWith(Sink.asPublisher(AsPublisher.WITH_FANOUT), mat);

        moduleConfigEngine.registerSource(Observable.fromPublisher(moduleService).concatMap(Observable::fromIterable));
    }
}
