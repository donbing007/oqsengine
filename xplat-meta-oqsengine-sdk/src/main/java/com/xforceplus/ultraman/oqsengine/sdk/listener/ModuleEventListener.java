package com.xforceplus.ultraman.oqsengine.sdk.listener;

import com.xforceplus.ultraman.metadata.grpc.Base;
import com.xforceplus.ultraman.metadata.grpc.CheckServiceClient;
import com.xforceplus.ultraman.metadata.grpc.ModuleUp;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.event.MetadataModuleGotEvent;
import com.xforceplus.ultraman.oqsengine.sdk.event.MetadataModuleVersionMissingEvent;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * module event listener
 */
public class ModuleEventListener {

    @Autowired
    private MetadataRepository store;

    @Autowired
    private CheckServiceClient checkServiceClient;

    @Autowired
    private AuthSearcherConfig config;

    @Autowired
    private MetadataRepository repository;

    Logger logger = LoggerFactory.getLogger(ModuleEventListener.class);

    @EventListener(MetadataModuleGotEvent.class)
    public void saveMetadata(MetadataModuleGotEvent event){
        logger.info("Got Module {}", event);
        store.save(event.getResponse(), event.getRequest().getTenantId(), event.getRequest().getAppId());

    }

    @Async
    @EventListener(MetadataModuleVersionMissingEvent.class)
    public void requestMetadata(MetadataModuleVersionMissingEvent event){

        Base.Authorization request = com.xforceplus
                .ultraman.metadata.grpc.Base.Authorization.newBuilder()
                .setAppId(config.getAppId())
                .setEnv(config.getEnv())
                .setTenantId(config.getTenant())
                .build();

        Long module = event.getModuleId();
        String version = event.getVersion();
        ModuleUpResult result = checkServiceClient.check(
                ModuleUp.newBuilder()
                .setModuleId(module.toString())
                .setModuleVersion(version)
                .addAuthorization(request)
                .build()).toCompletableFuture().join();

        repository.save(result, config.getTenant(), config.getAppId());
    }
}
