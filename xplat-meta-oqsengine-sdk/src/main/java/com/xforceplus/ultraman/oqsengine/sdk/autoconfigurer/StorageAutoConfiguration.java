package com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer;

import com.xforceplus.tower.storage.StorageFactory;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.configuration.StorageAppIdSupplier;
import com.xforceplus.ultraman.oqsengine.sdk.service.export.ExportSink;
import com.xforceplus.ultraman.oqsengine.sdk.service.impl.StorageSink;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * StorageAutoConfiguration
 */
@ConditionalOnProperty(value = "xplat.oqsengine.sdk.export.storage-sink", matchIfMissing = false)
@ConditionalOnClass(StorageFactory.class)
@Configuration
public class StorageAutoConfiguration {


    @ConditionalOnMissingBean(ExportSink.class)
    @ConditionalOnBean(StorageAppIdSupplier.class)
    @Bean
    public ExportSink storageFileSink(StorageFactory storageFactory, ContextService contextService, StorageAppIdSupplier storageAppIdSupplier) {
        return new StorageSink(storageFactory, contextService, storageAppIdSupplier.getStorageAppId());
    }
}
