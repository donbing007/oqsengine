package com.xforceplus.ultraman.oqsengine.sdk.config.init;

import akka.stream.ActorMaterializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.xforceplus.ultraman.config.ConfigurationEngine;
import com.xforceplus.ultraman.config.json.JsonConfigNode;
import com.xforceplus.ultraman.metadata.grpc.DictCheckServiceClient;
import com.xforceplus.ultraman.metadata.grpc.DictUpResult;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.event.config.ConfigChangeEvent;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import com.xforceplus.xplat.galaxy.grpc.client.LongConnect;
import io.reactivex.Observable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * dict grpc init service
 */
@Order
public class DictInitService implements InitializingBean {

    @Autowired
    private DictCheckServiceClient client;

    @Autowired
    private ActorMaterializer mat;

    @Autowired
    private AuthSearcherConfig config;

    private Logger logger = LoggerFactory.getLogger(DictInitService.class);

    @Autowired
    private ConfigurationEngine<DictUpResult, JsonConfigNode> dictConfigEngine;

    @Override
    public void afterPropertiesSet() throws Exception {
        com.xforceplus.ultraman.metadata.grpc.Base.Authorization request = com.xforceplus
                .ultraman.metadata.grpc.Base.Authorization.newBuilder()
                .setAppId(config.getAppId())
                .setTenantId(config.getTenant())
                .setEnv(config.getEnv())
                .build();

        Publisher<DictUpResult> dictPublisher = LongConnect.safeSource(2, 20
                , () -> client.checkStreaming(request))
                .log("DictService")
                .runWith(Sink.asPublisher(AsPublisher.WITH_FANOUT), mat);

        dictConfigEngine.registerSource(Observable.fromPublisher(dictPublisher));
    }
}
