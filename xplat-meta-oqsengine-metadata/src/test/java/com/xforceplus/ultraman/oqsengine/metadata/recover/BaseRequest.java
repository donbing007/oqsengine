package com.xforceplus.ultraman.oqsengine.metadata.recover;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

import com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.connect.MetaSyncGRpcClient;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * desc :.
 * name : BaseRequest
 *
 * @author : xujia 2021/4/7
 * @since : 1.8
 */
public class BaseRequest {

    protected IRequestHandler requestHandler;

    protected GRpcParams grpcParams;

    protected RequestWatchExecutor requestWatchExecutor;

    protected ExecutorService executorService;

    protected EntityClassSyncClient entityClassSyncClient;

    protected void baseInit() {
        grpcParams = Constant.grpcParamsConfig();

        requestWatchExecutor = requestWatchExecutor();

        requestHandler = requestHandler();

        entityClassSyncClient = entityClassSyncClient();
    }

    protected EntityClassSyncClient entityClassSyncClient() {

        MetaSyncGRpcClient metaSyncGrpcClient = new MetaSyncGRpcClient(Constant.HOST, Constant.PORT);
        ReflectionTestUtils.setField(metaSyncGrpcClient, "gRpcParams", grpcParams);

        EntityClassSyncClient entityClassSyncClient = new EntityClassSyncClient();

        ReflectionTestUtils.setField(entityClassSyncClient, "client", metaSyncGrpcClient);
        ReflectionTestUtils.setField(entityClassSyncClient, "requestHandler", requestHandler);
        ReflectionTestUtils.setField(entityClassSyncClient, "gRpcParamsConfig", grpcParams);

        return entityClassSyncClient;
    }

    protected RequestWatchExecutor requestWatchExecutor() {
        RequestWatchExecutor requestWatchExecutor = new RequestWatchExecutor();
        return requestWatchExecutor;
    }

    protected IRequestHandler requestHandler() {
        IRequestHandler requestHandler = new SyncRequestHandler();

        SyncExecutor syncExecutor = new SyncExecutor() {
            Map<String, Integer> stringIntegerMap = new HashMap<>();

            @Override
            public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
                stringIntegerMap.put(appId, version);
                return true;
            }

            @Override
            public int version(String appId) {
                Integer version = stringIntegerMap.get(appId);
                if (null == version) {
                    return NOT_EXIST_VERSION;
                }
                return version;
            }
        };

        executorService = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        ReflectionTestUtils.setField(requestHandler, "syncExecutor", syncExecutor);
        ReflectionTestUtils.setField(requestHandler, "requestWatchExecutor", requestWatchExecutor);
        ReflectionTestUtils.setField(requestHandler, "gRpcParams", grpcParams);
        ReflectionTestUtils.setField(requestHandler, "executorService", executorService);

        return requestHandler;
    }
}
