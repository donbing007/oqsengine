package com.xforceplus.ultraman.oqsengine.sdk.listener;

import com.xforceplus.ultraman.oqsengine.sdk.event.MetadataModuleGetEvent;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;


public class ModuleEventListener {

    @Autowired
    private MetadataRepository store;

    @EventListener(MetadataModuleGetEvent.class)
    public void saveMetadata(MetadataModuleGetEvent event){
        store.save(event.getResponse(), event.getRequest().getTenantId(), event.getRequest().getAppId());
    }
}
