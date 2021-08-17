package com.xforceplus.ultraman.oqsengine.core.service.integration.grpc.devops;

import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import io.grpc.stub.StreamObserver;
import java.lang.reflect.Field;
import java.util.Collection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by justin.xu on 08/2021.
 *
 * @since 1.8
 */
@Configuration
public class BeanConfiguration {
//
//    @Bean(name = "metaManager")
//    public MetaManager metaManager() throws IllegalAccessException {
//        MetaManager metaManager = new StorageMetaManager();
//
//        Collection<Field> cacheFields = ReflectionUtils.printAllMembers(metaManager);
//        ReflectionUtils.reflectionFieldValue(cacheFields, "cacheExecutor", metaManager,
//            MetaInitialization.getInstance().getCacheExecutor());
//        ReflectionUtils.reflectionFieldValue(cacheFields, "requestHandler", metaManager, new IRequestHandler() {
//            @Override
//            public void invoke(EntityClassSyncResponse entityClassSyncResponse, Void unused) {
//
//            }
//
//            @Override
//            public boolean isShutDown() {
//                return false;
//            }
//
//            @Override
//            public void start() {
//
//            }
//
//            @Override
//            public void stop() {
//
//            }
//
//            @Override
//            public boolean register(WatchElement watchElement) {
//                return false;
//            }
//
//            @Override
//            public boolean reRegister() {
//                return false;
//            }
//
//            @Override
//            public void initWatcher(String clientId, String uid,
//                                    StreamObserver<EntityClassSyncRequest> streamObserver) {
//
//            }
//
//            @Override
//            public IRequestWatchExecutor watchExecutor() {
//                return null;
//            }
//
//            @Override
//            public void notReady() {
//
//            }
//
//            @Override
//            public void ready() {
//
//            }
//        });
//        ReflectionUtils.reflectionFieldValue(cacheFields, "syncExecutor", metaManager,
//            MetaInitialization.getInstance().getEntityClassSyncExecutor());
//        ReflectionUtils.reflectionFieldValue(cacheFields, "asyncDispatcher", metaManager,
//            CommonInitialization.getInstance().getRunner());
//
//        return metaManager;
//    }
}
