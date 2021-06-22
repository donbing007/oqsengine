package com.xforceplus.ultraman.oqsengine.boot.config;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.EntityClassStorageHelper;
import com.xforceplus.ultraman.oqsengine.meta.handler.DoNothingRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.EntityClassSyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.executor.ExpireExecutor;
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
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('offline')")
    public MetaManager productMetaManager(@Value("${meta.grpc.type:offline}") String type) {
        StorageMetaManager storageMetaManager = new StorageMetaManager();
        if (type.equals("offline")) {
            storageMetaManager.isOffLineUse();
        }

        return storageMetaManager;
    }

    @Bean
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('offline')")
    public CacheExecutor cacheExecutor() {
        return new DefaultCacheExecutor();
    }

    /**
     * grpc同步执行器.
     */
    @Bean("grpcSyncExecutor")
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('offline')")
    public SyncExecutor grpcSyncExecutor(@Value("${meta.load.path:-}") String loadPath) {
        EntityClassSyncExecutor entityClassSyncExecutor = new EntityClassSyncExecutor();
        if (null != loadPath && !loadPath.isEmpty() && !loadPath.equals("-")) {
            logger.info("init entityClassSyncExecutor load-local-path : {}", loadPath);
            entityClassSyncExecutor.setLoadPath(loadPath);
        }
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
    @ConditionalOnExpression("'${meta.grpc.type}'.equals('client') || '${meta.grpc.type}'.equals('offline')")
    public IDelayTaskExecutor delayTaskExecutor() {
        return new ExpireExecutor();
    }
}
