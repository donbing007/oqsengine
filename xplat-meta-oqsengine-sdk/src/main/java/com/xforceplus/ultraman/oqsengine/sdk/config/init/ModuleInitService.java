package com.xforceplus.ultraman.oqsengine.sdk.config.init;

import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import com.xforceplus.ultraman.metadata.grpc.CheckServiceClient;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.event.MetadataModuleGotEvent;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.xplat.galaxy.grpc.client.LongConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * module grpc init service
 */
public class ModuleInitService implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(ModuleInitService.class);

    @Autowired
    CheckServiceClient checkServiceClient;

    @Autowired
    private ActorMaterializer mat;

    @Autowired
    private AuthSearcherConfig config;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    MetadataRepository store;

    @Override
    public void afterPropertiesSet() throws Exception {

        com.xforceplus.ultraman.metadata.grpc.Base.Authorization request = com.xforceplus
                .ultraman.metadata.grpc.Base.Authorization.newBuilder()
                .setAppId(config.getAppId())
                .setEnv(config.getEnv())
                .setTenantId(config.getTenant())
                .build();

        LongConnect.safeSource(2, 20
                , () -> checkServiceClient.checkStreaming(request))
                .runWith(Sink.foreach(x -> {
                    logger.debug("Got module {}", x);
                    //publisher.publishEvent();
                    MetadataModuleGotEvent event = new MetadataModuleGotEvent(request, x);
                    store.save(event.getResponse(), event.getRequest().getTenantId(), event.getRequest().getAppId());
                    logger.info("saved module ");
                }), mat);
    }
}
