package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassSyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.ExpireExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * meta manager.
 */
@Configuration
public class MetaManagerConfiguration {

    @Bean("metaManager")
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('server')")
    public MetaManager productMetaManager() {
        return new StorageMetaManager();
    }

    @Bean
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('server')")
    public CacheExecutor cacheExecutor() {
        return new DefaultCacheExecutor();
    }

    @Bean
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('server')")
    public SyncExecutor grpcSyncExecutor() {
        return new EntityClassSyncExecutor();
    }

    @Bean
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('server')")
    public IDelayTaskExecutor delayTaskExecutor() {
        return new ExpireExecutor();
    }

}
