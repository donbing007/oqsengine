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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    Logger logger = LoggerFactory.getLogger(ModuleEventListener.class);

    @Async
    @EventListener(MetadataModuleGotEvent.class)
    public void saveMetadata(MetadataModuleGotEvent event){


        List<ModuleUpResult> moduleUpResults = Optional.ofNullable(event.getResponse()).orElseGet(Collections::emptyList);

        moduleUpResults.forEach(module -> {
            logger.debug("Got Module {}", event);
            store.save(module, event.getRequest().getTenantId(), event.getRequest().getAppId());
            logger.info("Module {}:{} saved ", module.getId(), module.getCode());
        });
    }

    @Async
    @EventListener(MetadataModuleVersionMissingEvent.class)
    public void requestMetadata(MetadataModuleVersionMissingEvent event){

        logger.debug("Got Module Missing {}", event);
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

        logger.debug("Got Versioned Module {}", result);
        store.save(result, config.getTenant(), config.getAppId());
        logger.debug("Versioned Module saved {}", Optional.ofNullable(result).map(x -> x.getVersion()).orElseGet(() -> "none"));
    }
}
