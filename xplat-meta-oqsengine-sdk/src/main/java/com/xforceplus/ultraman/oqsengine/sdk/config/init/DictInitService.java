package com.xforceplus.ultraman.oqsengine.sdk.config.init;

import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import com.xforceplus.ultraman.metadata.grpc.DictCheckServiceClient;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import com.xforceplus.xplat.galaxy.grpc.client.LongConnect;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * dict grpc init service
 */
public class DictInitService implements InitializingBean {

    @Autowired
    private DictCheckServiceClient client;

    @Autowired
    private ActorMaterializer mat;

    @Autowired
    private AuthSearcherConfig config;

    @Autowired
    private DictMapLocalStore dictLocalStore;

    @Override
    public void afterPropertiesSet() throws Exception {

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
                    dictLocalStore.save(x, request.getTenantId(), request.getAppId());
                }), mat);
    }
}
