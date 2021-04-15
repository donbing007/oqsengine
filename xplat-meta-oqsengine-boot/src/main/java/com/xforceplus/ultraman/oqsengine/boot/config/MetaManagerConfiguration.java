package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.ICacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassSyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.ExpireExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * meta manager
 */
@Configuration
public class MetaManagerConfiguration {

    @Bean
    public MetaManager metaManager(){
        return new StorageMetaManager();
    }

    @Bean
    public ICacheExecutor cacheExecutor(){
        return new CacheExecutor();
    }

    @Bean
    public SyncExecutor grpcSyncExecutor(){
        return new EntityClassSyncExecutor();
    }

    @Bean
    public IDelayTaskExecutor iDelayTaskExecutor(){
        return new ExpireExecutor();
    }
}
