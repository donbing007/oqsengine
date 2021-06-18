package com.xforceplus.ultraman.oqsengine.boot.config;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.EntityClassStorageHelper;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassSyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.ExpireExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.mock.EnhancedSyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * meta manager.
 */
@Configuration
public class MetaManagerConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

    /**
     * 跟据metadata.enhanced生产是否带增强功能的SyncExecutor,默认false
     * 增加的SyncExecutor会记录bocp同步过来的原始数据内容，供测试项目进行assert比较.
     */
    @Bean
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('server')")
    public SyncExecutor grpcSyncExecutor(
        @Value("${metadata.enhanced:false}") boolean enhanced) {
        if (enhanced) {
            logger.info("init EnhancedSyncExecutor success.");
            return new EnhancedSyncExecutor();
        }
        logger.info("init EntityClassSyncExecutor success.");
        return new EntityClassSyncExecutor();
    }

    /**
     * 产生一个mock的syncExecutor.
     */
    @Bean
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('mock')")
    public SyncExecutor mockSyncExecutor() {
        return new SyncExecutor() {
            @Override
            public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
                return true;
            }

            @Override
            public boolean dataImport(String appId, int version, String content) {
                try {
                    EntityClassStorageHelper.toEntityClassSyncRspProto(content);
                    return true;
                } catch (InvalidProtocolBufferException e) {
                    logger.error("toEntityClassSyncRspProto error, message {}", e.getMessage());
                    return false;
                }
            }

            @Override
            public int version(String appId) {
                return 0;
            }
        };
    }

    @Bean
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('server')")
    public IDelayTaskExecutor delayTaskExecutor() {
        return new ExpireExecutor();
    }

}
