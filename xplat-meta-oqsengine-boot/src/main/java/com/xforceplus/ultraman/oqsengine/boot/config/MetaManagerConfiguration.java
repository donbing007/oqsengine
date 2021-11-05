package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.handler.DoNothingRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.AggregationEventBuilder;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassSyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.ExpireExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.handler.DefaultEntityClassFormatHandler;
import com.xforceplus.ultraman.oqsengine.metadata.handler.EntityClassFormatHandler;
import com.xforceplus.ultraman.oqsengine.metadata.mock.EnhancedSyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * meta manager.
 */
@Configuration
public class MetaManagerConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('offline')")
    public IRequestHandler requestHandler() {
        return new DoNothingRequestHandler();
    }

    /**
     * 初始化MetaManager.
     * 增加isOffLineUse.
     */
    @Bean("metaManager")
    @DependsOn({"cacheExecutor", "grpcSyncExecutor"})
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('offline')")
    public MetaManager productMetaManager(@Value("${meta.grpc.type:offline}") String type,
                                          @Value("${meta.load.path:}") String loadPath) {
        StorageMetaManager storageMetaManager = new StorageMetaManager();
        if (type.equals("offline")) {
            logger.info("init storageMetaManager, use offline model.");
            storageMetaManager.isOffLineUse();
        }
        logger.info("init storageMetaManager success.");

        if (null != loadPath && !loadPath.isEmpty() && !loadPath.equals("-")) {
            logger.info("init entityClassSyncExecutor load-local-path : {}", loadPath);
            storageMetaManager.setLoadPath(loadPath);
        }

        return storageMetaManager;
    }

    @Bean("cacheExecutor")
    @DependsOn("redisClient")
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('offline')")
    public CacheExecutor cacheExecutor() {
        logger.info("init cacheExecutor success.");
        return new DefaultCacheExecutor();
    }

    /**
     * 跟据metadata.enhanced生产是否带增强功能的SyncExecutor,默认false
     * 增加的SyncExecutor会记录bocp同步过来的原始数据内容，供测试项目进行assert比较.
     */
    @Bean("grpcSyncExecutor")
    @DependsOn("autoFillUpgradeListener")
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('offline')")
    public SyncExecutor grpcSyncExecutor(
        @Value("${metadata.enhanced:false}") boolean enhanced) {
        if (enhanced) {
            logger.info("init EnhancedSyncExecutor success.");
            return new EnhancedSyncExecutor();
        }
        logger.info("init EntityClassSyncExecutor success.");
        EntityClassSyncExecutor entityClassSyncExecutor = new EntityClassSyncExecutor();

        return entityClassSyncExecutor;
    }

    /**
     * 产生一个mock的syncExecutor.
     */
    @Bean("grpcSyncExecutor")
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('mock')")
    public SyncExecutor mockSyncExecutor() {
        return new SyncExecutor() {
            @Override
            public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
                return true;
            }

            @Override
            public int version(String appId) {
                return 0;
            }
        };
    }

    @Bean
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('offline')")
    public IDelayTaskExecutor delayTaskExecutor() {
        return new ExpireExecutor();
    }

    @Bean("entityClassFormatHandler")
    public EntityClassFormatHandler entityClassFormatHandler() {
        return new DefaultEntityClassFormatHandler();
    }
}
