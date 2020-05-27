package com.xforceplus.ultraman.oqsengine.sdk.config.init;

import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import com.xforceplus.ultraman.metadata.grpc.DictCheckServiceClient;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import com.xforceplus.xplat.galaxy.grpc.client.LongConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * dict grpc init service
 */
public class DictInitService implements SmartInitializingSingleton {

    @Autowired
    private DictCheckServiceClient client;

    @Autowired
    private ActorMaterializer mat;

    @Autowired
    private AuthSearcherConfig config;

    @Autowired
    private DictMapLocalStore dictLocalStore;

    private Logger logger = LoggerFactory.getLogger(DictInitService.class);

    @Override
    public void afterSingletonsInstantiated() {
        com.xforceplus.ultraman.metadata.grpc.Base.Authorization request = com.xforceplus
                .ultraman.metadata.grpc.Base.Authorization.newBuilder()
                .setAppId(config.getAppId())
                .setTenantId(config.getTenant())
                .setEnv(config.getEnv())
                .build();

        LongConnect.safeSource(2, 20
                , () -> client.checkStreaming(request))
                .log("DictService")
                .runWith(Sink.foreach(x -> {
                    dictLocalStore.save(x, request.getAppId());
                    if (logger.isInfoEnabled()) {
                        x.getDictsList().forEach(dict -> {
                            logger.info("Dict {}:{}:{} saved", dict.getCode(), dict.getPublishDictId(), dict.getId());
                        });
                    }
                }), mat);
    }
}
